package me.rand0m.cloudwatch.entities;

import java.util.List;

public interface ResourceDiscovery {
	
	public List<Resource> getResources(TargetAccount account); 
	
}
