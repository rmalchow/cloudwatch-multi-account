package me.rand0m.cloudwatch.aws.sts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import me.rand0m.cloudwatch.entities.TargetAccount;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

@Component
public class StsService {

	@Autowired
	private StsClientHelper sts;
	
	public String getAccountId(TargetAccount account) {
		StsClient c = StsClient.builder().credentialsProvider(sts.assumeRoleProvider(account)).region(Region.of(account.getRegion())).build();
		GetCallerIdentityResponse response = c.getCallerIdentity();
		return response.account();
	}
	
}
