package me.rand0m.cloudwatch.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Child;

public class CWMetric {

	private Config config;
	private CWMetricConfig metricConfig;
	private CollectorRegistry registry;
	private Gauge gauge;
	
	public CWMetric(Config config, CWMetricConfig metricConfig, CollectorRegistry registry) {
		this.setConfig(config);
		this.setMetricConfig(metricConfig);
		this.registry = registry;
		Gauge.Builder gb = Gauge.build();
		gb.help("[no help]");
		List<String> labelNames = new ArrayList<>();
		labelNames.add("entity");
		labelNames.addAll(config.getTags());
		String[] ln = labelNames.toArray(new String[] {});
		gb.labelNames(ln);
		gb.name(metricConfig.getName());
		gauge = gb.create();
		gauge.register(this.registry);
	}
	
	public Child get(String... labels) {
		return gauge.labels(labels);
	}

	public CWMetricConfig getMetricConfig() {
		return metricConfig;
	}

	public void setMetricConfig(CWMetricConfig config) {
		this.metricConfig = config;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	
}
