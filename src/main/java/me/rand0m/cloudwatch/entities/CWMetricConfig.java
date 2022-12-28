package me.rand0m.cloudwatch.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CWMetricConfig {

	private String name;
	private String awsNamespace;
	private String awsType;
	private int period;
	private String awsMetricName;
	private String awsStatistic;
	private List<String> awsDimensions = new ArrayList<>();

	public String getAwsNamespace() {
		return awsNamespace;
	}

	public void setAwsNamespace(String awsNamespace) {
		this.awsNamespace = awsNamespace;
	}

	public String getAwsMetricName() {
		return awsMetricName;
	}

	public void setAwsMetricName(String awsMetricName) {
		this.awsMetricName = awsMetricName;
	}

	public List<String> getAwsDimensions() {
		return awsDimensions;
	}

	public void setAwsDimensions(List<String> awsDimensions) {
		this.awsDimensions = awsDimensions;
	}
	
	@Override
	public boolean equals(Object that) {
		if(that instanceof CWMetricConfig) {
			return this.hashCode() == that.hashCode(); 
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int hashCode() {
		int h = 0;
		for (List<Object> ss : (List<Object>[])new List[] { 
				awsDimensions, 
				Collections.singletonList(awsStatistic), 
				Collections.singletonList(awsMetricName), 
				Collections.singletonList(awsNamespace), 
				Collections.singletonList(awsType) 
			} ) {
			for(Object s : ss) {
				if(s!=null) {
					h = h ^ s.hashCode();
				}
			}
		}
		return h;
	}

	public String getAwsType() {
		return awsType;
	}

	public void setAwsType(String awsType) {
		this.awsType = awsType;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public String getAwsStatistic() {
		return awsStatistic;
	}

	public void setAwsStatistic(String awsStatistic) {
		this.awsStatistic = awsStatistic;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
