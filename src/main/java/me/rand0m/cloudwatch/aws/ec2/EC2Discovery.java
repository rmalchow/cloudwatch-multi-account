package me.rand0m.cloudwatch.aws.ec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import me.rand0m.cloudwatch.api.ResourceDiscovery;
import me.rand0m.cloudwatch.aws.sts.StsClientHelper;
import me.rand0m.cloudwatch.aws.sts.StsService;
import me.rand0m.cloudwatch.entities.Resource;
import me.rand0m.cloudwatch.entities.TargetAccount;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Tag;

@Component
public class EC2Discovery implements ResourceDiscovery {

	private static Log log = LogFactory.getLog(EC2Discovery.class);

	@Autowired
	private StsService stsService;

	@Autowired
	private StsClientHelper sts;

	@Override
	public List<Resource> getResources(TargetAccount account) {
		Ec2Client c = Ec2Client.builder().credentialsProvider(sts.assumeRoleProvider(account)).region(Region.of(account.getRegion())).build();
		List<Resource> out = new ArrayList<>();

		Map<String, String> shared = new HashMap<>();
		shared.put("AccountID", stsService.getAccountId(account));
		shared.put("Region", account.getRegion());

		out.addAll(getEc2(c, shared));
		return out;
	}

	private List<Resource> getEc2(Ec2Client c, Map<String, String> shared) {
		List<Resource> out = new ArrayList<>();
		try {
			DescribeInstancesRequest.Builder b = DescribeInstancesRequest.builder();
			DescribeInstancesResponse res = null;
			String t = null;
			do {
				DescribeInstancesRequest req = b.build();
				res = c.describeInstances(req);
				for (Reservation r : res.reservations()) {
					for (Instance a : r.instances()) {
						Resource resource = new Resource();
						resource.setNamespace("AWS/EC2");
						resource.setType("ec2");
						resource.setId(a.instanceId());
						Dimension.Builder db = Dimension.builder();
						db.name("InstanceId");
						db.value(resource.getId());
						resource.setIdentityDimension(db.build());
						
						for (Tag tag : a.tags()) {
							resource.addLabel(tag.key(), tag.value());
						}
						for (Map.Entry<String, String> m : shared.entrySet()) {
							resource.addLabel(m.getKey(), m.getValue());
						}
						out.add(resource);
					}
				}
				t = res.nextToken();
				b.nextToken(t);
			} while (t != null);
		} catch (Exception e) {
			log.warn("error in ec2 discovery: ", e);
		}
		log.info("found EC2 instances: "+out.size());
		return out;
	}

}
