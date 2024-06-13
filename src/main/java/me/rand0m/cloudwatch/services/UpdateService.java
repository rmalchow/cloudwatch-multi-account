package me.rand0m.cloudwatch.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.prometheus.client.CollectorRegistry;
import jakarta.annotation.PostConstruct;
import me.rand0m.cloudwatch.api.ResourceDiscovery;
import me.rand0m.cloudwatch.aws.cloudwatch.GetMetricDataDataGetter;
import me.rand0m.cloudwatch.entities.CWMetric;
import me.rand0m.cloudwatch.entities.CWMetricConfig;
import me.rand0m.cloudwatch.entities.CWMetricsInstance;
import me.rand0m.cloudwatch.entities.Config;
import me.rand0m.cloudwatch.entities.Resource;
import me.rand0m.cloudwatch.entities.TargetAccount;

@Service
public class UpdateService {

	private static Log log = LogFactory.getLog(UpdateService.class);
	
	@Autowired
	private List<ResourceDiscovery> discoveries;
	
	@Autowired
	private Config config;
	
	@Autowired
	private GetMetricDataDataGetter dataGetter;
	
	@PostConstruct
	public void init() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
			log.info("config: "+json);
		} catch (JsonProcessingException e) {
			log.warn("error updating resources: ",e);
		}
	}
	
	
	
	
	public CollectorRegistry run() {
		
		Collection<TargetAccount> accounts = config.getAccounts().values();

		log.info(">>> running scrape ("+accounts.size()+") accounts");
		long x = System.currentTimeMillis();
		CollectorRegistry reg = new CollectorRegistry();
		try {
			List<CWMetric> metrics = new ArrayList<>();
			
			for(CWMetricConfig mc : config.getMetrics().values()) {
				metrics.add(new CWMetric(config, mc, reg));
			}
			
			ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(16);

			List<Future<?>> futures = new ArrayList<>();
			
			for (TargetAccount ta : accounts) {
				
				List<CWMetricsInstance> mi = new ArrayList<>();
				for(Resource r : ta.getResources()) {
					mi.addAll(r.addRules(metrics));
				}
				
				Future<?> f = exec.submit(new AccountRunnable(ta, mi));
				futures.add(f);
			}
			
			while(futures.size()>0) {
				futures.remove(0).get(60,TimeUnit.SECONDS);
			}
			exec.shutdown();
			log.info(">>> scrape completed: "+(System.currentTimeMillis()-x)+"ms");
			
		} catch (Exception e) {
			log.warn(">>> ERROR running scrape",e);
		}
		return reg;
	}
	
	@PostConstruct
	@Scheduled(initialDelayString = "${discoverInterval:360000}", fixedDelayString = "${discoverInterval:360000}")
	public void discover() {
		log.info(">>> running discovery");
		for (TargetAccount ta : config.getAccounts().values()) {
			List<Resource> res = new ArrayList<>();
			log.info(ta.getRole()+" / "+ta.getRegion()+" - "+discoveries.size()+" discoveries");
			for (ResourceDiscovery d : discoveries) {
				try {
					log.info(d.getClass().getSimpleName()+" / "+ta.getRole()+" / "+ta.getRegion());
					for(Resource r : d.getResources(ta)) {
						res.add(r);
					}
				} catch(Exception e) {
					log.warn("error updating resources: ",e);
				}
			}
			ta.setResources(res);
		}
		log.info("got accounts: "+config.getAccounts().size());
	}
	
	private class AccountRunnable implements Runnable {
		
		private TargetAccount ta;
		private Collection<CWMetricsInstance> instances;

		public AccountRunnable(TargetAccount ta, Collection<CWMetricsInstance> instances) {
			super();
			this.ta = ta;
			this.instances = instances;
		}

		@Override
		public void run() {
			try {
				log.info(">>> running scrape accounts: "+ta.getName());
				dataGetter.updateMetrics(ta,instances);
			} catch (Exception e) {
				log.warn("error updating data: ",e);
			}
		}
		
		
		
		
	}
	
	
}
