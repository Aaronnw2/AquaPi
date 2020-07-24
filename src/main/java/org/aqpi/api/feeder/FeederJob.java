package org.aqpi.api.feeder;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class FeederJob  implements Job {

	private static final Logger LOG = LoggerFactory.getLogger(FeederJob.class);
	
	@Autowired
	private FeederDelegate delegate;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.info("Running scheduled Feeding");
		try {
			this.delegate.feedNow();
		} catch (InterruptedException e) {
			LOG.error("Feeder job was innterrupted", e);
		}
	}

}
