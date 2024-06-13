package me.rand0m.cloudwatch.aws.asg;

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
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

@Component
public class ASGDiscovery implements ResourceDiscovery {
	
	private static Log log = LogFactory.getLog(ASGDiscovery.class);

	@Autowired
	private StsService stsService;

	@Autowired
	private StsClientHelper sts;

	@Override
	public List<Resource> getResources(TargetAccount account) {
		AutoScalingClient c = AutoScalingClient.builder().credentialsProvider(sts.assumeRoleProvider(account)).region(Region.of(account.getRegion())).build();
		List<Resource> out = new ArrayList<>();

		Map<String, String> shared = new HashMap<>();
		shared.put("AccountID", stsService.getAccountId(account));
		shared.put("Region", account.getRegion());

		out.addAll(getAsgs(c, shared));
		return out;
	}

	private List<Resource> getAsgs(AutoScalingClient c, Map<String, String> shared) {
		List<Resource> out = new ArrayList<>();
		try {
			DescribeAutoScalingGroupsRequest.Builder b = DescribeAutoScalingGroupsRequest.builder();
			DescribeAutoScalingGroupsResponse res = null;
			String t = null;
			do {
				DescribeAutoScalingGroupsRequest req = b.build();
				res = c.describeAutoScalingGroups(req);
				for (AutoScalingGroup r : res.autoScalingGroups()) {
					Resource resource = new Resource();
					resource.setNamespace("AWS/AutoScaling");
					resource.setType("asg");
					resource.setId(r.autoScalingGroupName());

					Dimension.Builder db = Dimension.builder();
					db.name("AutoScalingGroupName");
					db.value(resource.getId());
					resource.setIdentityDimension(db.build());
					
					for (Map.Entry<String, String> m : shared.entrySet()) {
						resource.addLabel(m.getKey(), m.getValue());
					}
					out.add(resource);
				}
				t = res.nextToken();
				b.nextToken(t);
			} while (t != null);
		} catch (Exception e) {
			log.warn("error in asg discovery: ", e);
		}
		log.info("found ASG instances: "+out.size());
		return out;
	}

}
