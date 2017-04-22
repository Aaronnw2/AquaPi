package org.aqpi;

import javax.annotation.PreDestroy;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AquaPiApplication {

	@Autowired
	private Scheduler scheduler;
	
	public static void main(String[] args) {
		SpringApplication.run(AquaPiApplication.class, args);
	}
	
	@PreDestroy
	public void cleanup() throws SchedulerException {
		scheduler.shutdown();
	}
}
