package me.rand0m.cloudwatch.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TargetAccount {

	private static Log log = LogFactory.getLog(TargetAccount.class);

	private String name;
	private String region;
	private String accessKey;
	private String secretKey;
	private String profile;
	private String role;

	private List<Resource> resources = new ArrayList<>();
	
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public void setResources(List<Resource> resources) {
		Map<String,Resource> existing = toMap(this.resources); 
		Map<String,Resource> add = toMap(resources);
		int csame=0;
		int cadd=0;
		int cremove=0;
		
		for(String id : new ArrayList<String>(existing.keySet())) {
			csame = csame + 1;
			add.remove(id);
			existing.remove(id);
		}
		for(Resource r : add.values()) {
			cadd = cadd + 1;
			this.resources.add(r);
		}
		for(Resource r : existing.values()) {
			cremove = cremove + 1;
			this.resources.remove(r);
		}
		log.info("account update: "+cadd+" added, "+cremove+" removed, "+csame+" unchanged");
	}
	
	private Map<String, Resource> toMap(List<Resource> rs) {
		Map<String, Resource> out = new HashMap<>();
		for(Resource r : rs) {
			out.put(r.getId(), r);
		}
		return out;
	}

	public List<Resource> getResources() {
		return Collections.unmodifiableList(resources);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
