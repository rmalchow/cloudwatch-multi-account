package me.rand0m.cloudwatch.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties()
public class Config {

	private Map<String,CWMetricConfig> metrics = new HashMap<>();
	private Map<String,TargetAccount> accounts = new HashMap<>();

	private List<String> tags = new ArrayList<>();
	private int queryStart = -10;
	private int queryEnd   = 0;

	public Map<String,TargetAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(Map<String,TargetAccount> accounts) {
		this.accounts.clear();
		this.accounts.putAll(accounts);
		for(Map.Entry<String, TargetAccount> e : this.accounts.entrySet()) {
			e.getValue().setName(e.getKey());
		}
	}

	public Map<String,CWMetricConfig> getMetrics() {
		return metrics;
	}

	public void setMetrics(Map<String,CWMetricConfig> metrics) {
		this.metrics.clear();
		this.metrics.putAll(metrics);
		for(Map.Entry<String, CWMetricConfig> e : this.metrics.entrySet()) {
			e.getValue().setName(e.getKey());
		}
	}

	public int getQueryStart() {
		return queryStart;
	}

	public void setQueryStart(int queryStart) {
		this.queryStart = queryStart;
	}

	public int getQueryEnd() {
		return queryEnd;
	}

	public void setQueryEnd(int queryEnd) {
		this.queryEnd = queryEnd;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
}
