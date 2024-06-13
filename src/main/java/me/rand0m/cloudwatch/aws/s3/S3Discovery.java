package me.rand0m.cloudwatch.aws.s3;

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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

@Component
public class S3Discovery implements ResourceDiscovery {

	private static Log log = LogFactory.getLog(S3Discovery.class);

	@Autowired
	private StsService stsService;

	@Autowired
	private StsClientHelper sts;

	@Override
	public List<Resource> getResources(TargetAccount account) {
		S3Client c = S3Client.builder().credentialsProvider(sts.assumeRoleProvider(account)).region(Region.of(account.getRegion())).build();
		List<Resource> out = new ArrayList<>();

		Map<String, String> shared = new HashMap<>();
		shared.put("AccountID", stsService.getAccountId(account));
		shared.put("Region", account.getRegion());

		out.addAll(getS3(c, shared));
		return out;
	}

	private List<Resource> getS3(S3Client c, Map<String, String> shared) {
		List<Resource> out = new ArrayList<>();
		try {
			ListBucketsResponse res = null;
			ListBucketsRequest r = ListBucketsRequest.builder().build();
			res = c.listBuckets(r);
			for(Bucket bucket : res.buckets()) {
				Resource resource = new Resource();
				resource.setNamespace("AWS/S3");
				resource.setType("s3");
				resource.setId(bucket.name());
				Dimension.Builder db = Dimension.builder();
				db.name("SourceBucket");
				db.value(resource.getId());
				resource.setIdentityDimension(db.build());
				out.add(resource);
			}
		} catch (Exception e) {
			log.warn("error in S3 discovery: ", e);
		}
		log.info("found S3 buckets: "+out.size());
		return out;
	}

}
