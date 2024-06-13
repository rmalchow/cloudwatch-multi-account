package me.rand0m.cloudwatch.api;

import java.util.List;

import me.rand0m.cloudwatch.entities.Resource;
import me.rand0m.cloudwatch.entities.TargetAccount;

public interface ResourceDiscovery {
	
	public List<Resource> getResources(TargetAccount account); 
	
}
