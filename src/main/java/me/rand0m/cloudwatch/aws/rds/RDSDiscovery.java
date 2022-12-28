package me.rand0m.cloudwatch.aws.rds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import me.rand0m.cloudwatch.aws.sts.StsClientHelper;
import me.rand0m.cloudwatch.aws.sts.StsService;
import me.rand0m.cloudwatch.entities.Resource;
import me.rand0m.cloudwatch.entities.ResourceDiscovery;
import me.rand0m.cloudwatch.entities.TargetAccount;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.Tag;

@Component
public class RDSDiscovery implements ResourceDiscovery {
	
	private static Log log = LogFactory.getLog(RDSDiscovery.class);
	
	@Autowired
	private StsService stsService;

	@Autowired
	private StsClientHelper sts;

	
	@Override
	public List<Resource> getResources(TargetAccount account) {
		RdsClient c = RdsClient.builder().credentialsProvider(sts.assumeRoleProvider(account)).region(Region.of(account.getRegion())).build();

		List<Resource> out = new ArrayList<>();
		
		Map<String,String> shared = new HashMap<>();
		shared.put("AccountID",stsService.getAccountId(account));
		shared.put("Region",account.getRegion());
		
		out.addAll(getRds(c, shared));
		return out;
	}


	private List<Resource> getRds(RdsClient c, Map<String,String> shared) {
		List<Resource> out = new ArrayList<>();
		try {
			DescribeDbInstancesRequest.Builder b = DescribeDbInstancesRequest.builder(); 
			DescribeDbInstancesResponse res = null;  
			String t = null;
			do {
				DescribeDbInstancesRequest req = b.build();
				res = c.describeDBInstances(req);
				
				for(DBInstance i : res.dbInstances()) {

					Resource resource = new Resource();
					
					resource.setNamespace("AWS/RDS");
					resource.setType("instance");
					resource.setId(i.dbInstanceIdentifier());
					
					Dimension.Builder db = Dimension.builder();
					db.name("DBInstanceIdentifier");
					db.value(resource.getId());
					resource.setIdentityDimension(db.build());
					
					for(Tag tag : i.tagList()) {
						resource.addLabel(tag.key(),tag.value());
					}
					
					for(Map.Entry<String, String> m : shared.entrySet()) {
						resource.addLabel(m.getKey(),m.getValue());
					}
					
					out.add(resource);
				}
				t = res.marker();
				b.marker(t);
			} while(t!=null);
		} catch(Exception e) {
			log.warn("error in ec2 discovery: ",e);
		}
		log.info("found RDS instances: "+out.size());
		return out;
	}

}
