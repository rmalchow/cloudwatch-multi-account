package me.rand0m.cloudwatch.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.cloudwatch.model.Dimension;

public class Resource {
	
	private String namespace;
	private String type;
	private String id;
	private Map<String,String> labels = new HashMap<>();
	private Dimension identityDimension;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public Map<String,String> getLabels() {
		return new HashMap<>(labels);
	}
	
	public void addLabel(String key, String value) {
		labels.put(key, value);
	}

	public List<CWMetricsInstance> addRules(Collection<CWMetric> metrics) {
		List<CWMetricsInstance> out = new ArrayList<>();
		for(CWMetric m : metrics) {
			if(!m.getMetricConfig().getAwsNamespace().equals(getNamespace())) {
				continue;
			} else if(!m.getMetricConfig().getAwsType().equals(getType())) {
				continue;
			} else {
				CWMetricsInstance mi = new CWMetricsInstance();
				mi.setRule(m);
				
				List<String> l = new ArrayList<>();
				l.add(getId());
				for(String s : m.getConfig().getTags()) {
					l.add(labels.get(s));
				}
				mi.setLabels(l);
				mi.setDimension(getIdentityDimension());
				out.add(mi);
			}
		}
		return out;
	}

	public Dimension getIdentityDimensions() {
		Dimension.Builder db = Dimension.builder();
		if(namespace.equalsIgnoreCase("AWS/EC2") && type.equals("ec2")) {
			db.name("InstanceId");
			db.value(getId());
		} else if(namespace.equalsIgnoreCase("AWS/RDS") && type.equals("instance")) {
			db.name("DBInstanceIdentifier");
			db.value(getId());
		}
			
		
		return db.build();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Dimension getIdentityDimension() {
		return identityDimension;
	}

	public void setIdentityDimension(Dimension identityDimension) {
		this.identityDimension = identityDimension;
	}

	
}
