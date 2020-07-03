package org.aqpi.purge;

import org.aqpi.temperature.TemperatureMonitorDelegate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PurgeTemperatureHistoryJob implements Job {

	@Autowired
	private TemperatureMonitorDelegate delegate;
	
	private static final Logger LOG = LoggerFactory.getLogger(PurgeTemperatureHistoryJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		delegate.purgeOldTemperatureHistory();
		LOG.info("Temperature data purged for records before yesterday");
	}

}
