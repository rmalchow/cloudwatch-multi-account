package me.rand0m.cloudwatch.aws.cloudwatch;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import jakarta.annotation.PostConstruct;
import me.rand0m.cloudwatch.aws.sts.StsClientHelper;
import me.rand0m.cloudwatch.entities.CWMetricsInstance;
import me.rand0m.cloudwatch.entities.Config;
import me.rand0m.cloudwatch.entities.TargetAccount;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;


@Component
public class GetMetricDataDataGetter {
	
	private static Log log = LogFactory.getLog(GetMetricDataDataGetter.class);

	@Autowired
	private StsClientHelper sts;

	@Autowired
	private Config config;
	
	private Counter apiRequests;
	private Counter metricsCount;
	
	@PostConstruct
	public void init() {
		Counter.Builder arb = Counter.build();
		arb.name("apiRequestCounter");
		arb.labelNames("role","region");
		arb.help("[no help]");
		
		apiRequests = arb.create();
		apiRequests.register(CollectorRegistry.defaultRegistry);
		
		Counter.Builder mcb = Counter.build();
		mcb.name("metricsCounter");
		mcb.labelNames("role","region");
		mcb.help("[no help]");
		
		metricsCount = mcb.create();
		metricsCount.register(CollectorRegistry.defaultRegistry);
		
	}
	
	public void updateMetrics(TargetAccount account, Collection<CWMetricsInstance> mis) {
		
		Map<String,CWMetricsInstance> miMap = new HashMap<>();
		List<MetricDataQuery> qs = new ArrayList<>();
		for(CWMetricsInstance mh: mis) {
			miMap.put(mh.getId(), mh);
			qs.add(mh.getQuery());
		}
		
		if(qs.size()<1) return;

		List<MetricDataResult> results = new ArrayList<>();
		
		try {
	
			CloudWatchClient c = CloudWatchClient.builder().credentialsProvider(sts.assumeRoleProvider(account)).region(Region.of(account.getRegion())).build();
			GetMetricDataRequest.Builder b = GetMetricDataRequest.builder();
			Instant end = Instant.now().plus(config.getQueryEnd(), ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
			Instant start = Instant.now().plus(config.getQueryStart(), ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS);
			b.startTime(start);
			b.endTime(end);
			{
				List<MetricDataQuery> n = new ArrayList<MetricDataQuery>();
				while(qs.size()>0 && n.size()<499) {
					n.add(qs.remove(0));
				}
				b.metricDataQueries(n);
			}
			
			while(true) {
				GetMetricDataResponse response = c.getMetricData(b.build());
				apiRequests.labels(account.getRole(),account.getRegion()).inc();
				results.addAll(response.metricDataResults());
				metricsCount.labels(account.getRole(),account.getRegion()).inc(response.metricDataResults().size());
				
				if(response.nextToken()!=null) {
					b.nextToken(response.nextToken());
				} else if (qs.size()>0) {
					List<MetricDataQuery> n = new ArrayList<MetricDataQuery>();
					while(qs.size()>0 && n.size()<499) {
						n.add(qs.remove(0));
					}
					b.metricDataQueries(n);
					b.nextToken(null);
				} else {
					break;
				}
			}
		} catch (Exception e) {
			log.error("error getting metrics data",e);
		}

		for(MetricDataResult res : results) {
			CWMetricsInstance h = miMap.remove(res.id());
			if(h==null) {
				log.warn("received metrics for unknown query: "+res.id());
			}
			Double d = getLatest(res);
			if(d != null) {
				h.update(d);
			} else {
				log.warn("received no metrics for: "+h.getMetricName()+" / "+h.getAwsMetricName()+" / "+h.getId()+" ("+res.values().size()+")");
			}
		}
		
		for(CWMetricsInstance mi : miMap.values()) {
			log.warn("did not receive a result for: "+mi.getMetricName()+" / "+mi.getAwsMetricName()+" / "+mi.getId());
		}
	}

	public Double getLatest(MetricDataResult res) {
		if(res.values().size()<1) {
			return null;
		}
		if(res.values().size()==1) {
			return res.values().get(0);
		}
		Instant ci = null;
		for(Instant i : res.timestamps()) {
			if(ci == null || ci.isBefore(i)) {
				ci = i;
			}
		}
		return res.values().get(res.timestamps().indexOf(ci));
	}
	
	
	
}
