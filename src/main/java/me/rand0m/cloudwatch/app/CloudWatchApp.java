package me.rand0m.cloudwatch.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages={"me.rand0m"})
@EnableScheduling
public class CloudWatchApp {

	public static void main(String[] args) {
	    SpringApplication.run(CloudWatchApp.class, args);
	}
	
}
