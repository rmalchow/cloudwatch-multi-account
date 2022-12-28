

# Multi-account cloudwatch exporter



## Introduction

I was looking for a way to make cloudwatch metrics accessible, and came across the "official" one:

https://github.com/prometheus/cloudwatch_exporter

this has a number of drawbacks - especially that it cannot do more metrics in one go. this does not really fit my use case. 



## Improvements

this exporter can

- discover resources (EC2 / RDS) in multiple accounts / regions
- scrape the appropriate metrics from them 

that's it, really.



## Config

there are some main sections in the config. on the top level, there:

### interval 

the number of milliseconds between resource discovery runs, i.e. the time between calls to "ec2:describeInstances" and "rds:describeInstances". newly added resources are not discovered until the next run.

note that this does not check the state of the resource, it will be scraped regardless of the state it is in.

### queryStart / queryEnd 

timeframe - relative to "now" - used in the metrics query to set the timeframe to retrieve metrics for. if this is too short, you may get empty results. 

### tags

these tags are copied from the AWS resources into the labels. note that the first label is always "entity" and the corresponding label value the ID of the AWS resource, such as "i-112345667" (ec2 instance id).

### accounts

this is a map of "account" / region combinations, the "key" being the name, and the value containing the IAM access configuration and the AWS region to use.

the values are:

- region: the region to use for api calls
- profile: equivalent of using the "AWS_PROFILE" environment variable
- access_key_id: equivalent of using the "AWS_ACCESS_KEY_ID" environment variable
- secret_access_key: equivalent of using the "AWS_SECRET_ACCESS_KEY" environment variable
- role: if set, perform a role switch from whatever is configured above

### metrics

these are the metrics to scrape. "AWS Namespace" and "type" are matched against the namespace and type of each resource. if they match, the given metric is scraped for the matching resource.


EC2 instance |  AWS/EC2  | instance
RDS instance |  AWS/