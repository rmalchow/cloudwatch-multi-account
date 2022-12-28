package me.rand0m.cloudwatch.aws.sts;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import me.rand0m.cloudwatch.entities.TargetAccount;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

@Component
public class StsClientHelper {
	
	public AwsCredentialsProvider assumeRoleProvider(TargetAccount account) {

		AwsCredentialsProviderChain.Builder b = AwsCredentialsProviderChain.builder();
		b.addCredentialsProvider(DefaultCredentialsProvider.create());
		
		if(StringUtils.hasText(account.getProfile())) {
			b.addCredentialsProvider(ProfileCredentialsProvider.create(account.getProfile()));
		} else if(StringUtils.hasText(account.getAccessKey())) {
			b.addCredentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(account.getAccessKey(), account.getSecretKey())));
		}
		
		if(StringUtils.hasText(account.getRole())) {

			StsClientBuilder sts = StsClient.builder();
			sts.credentialsProvider(b.build());
			sts.region(Region.of(account.getRegion()));
			
			return StsAssumeRoleCredentialsProvider
				.builder()
				.stsClient(sts.build())
				.refreshRequest(
					() -> AssumeRoleRequest
		            .builder()
		            .roleArn(account.getRole())
		            .roleSessionName("scrape")
		            .build()
					)
				.build();
		}
		return b.build();
	}
}
