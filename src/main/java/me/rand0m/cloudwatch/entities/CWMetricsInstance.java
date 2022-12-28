package me.rand0m.cloudwatch.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;

public class CWMetricsInstance {
	
	private CWMetric metric;
	private String id = "id"+UUID.randomUUID().toString().replace("-", "");
	private Dimension dimension;
	private Collection<String> labels = new ArrayList<>(); 
	
	public CWMetricsInstance() {
	}
	
	public String getAwsMetricName() {
		return metric.getMetricConfig().getAwsMetricName();
	}
	
	public String getMetricName() {
		return metric.getMetricConfig().getName();
	}
	
	public void update(Double v) {
		if(v!=null) {
			String[] l = labels.toArray(new String[0]);
			metric.get(l).set(v.doubleValue());
		}
	}
	
	public void setLabels(List<String> labels) {
		this.labels.clear();
		for(String l : labels) {
			this.labels.add(l==null?"[NONE]":l);
		}
	}
	
	public void destroy() {
	}

	public void setRule(CWMetric metric) {
		this.metric = metric;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MetricDataQuery getQuery() {
		
		Metric.Builder mb = Metric.builder();
		mb.metricName(metric.getMetricConfig().getAwsMetricName());
		mb.namespace(metric.getMetricConfig().getAwsNamespace());
		mb.dimensions(dimension);

		MetricStat.Builder sb = MetricStat.builder();
		sb.period(metric.getMetricConfig().getPeriod());
		sb.stat(metric.getMetricConfig().getAwsStatistic());
		sb.metric(mb.build());
		
		MetricDataQuery.Builder qb = MetricDataQuery.builder();
		qb.returnData(true);
		qb.id(getId());
		qb.metricStat(sb.build());

		return qb.build();
	}

	public Dimension getDimension() {
		return dimension;
	}

	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}

}
