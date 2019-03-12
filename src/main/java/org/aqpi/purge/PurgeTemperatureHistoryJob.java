package org.aqpi.purge;

import javax.transaction.Transactional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aqpi.temperature.TemperatureMonitorDelegate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class PurgeTemperatureHistoryJob implements Job {

	@Autowired
	private TemperatureMonitorDelegate delegate;
	
	private static final Logger LOG = LogManager.getLogger(PurgeTemperatureHistoryJob.class);
	
	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		delegate.purgeOldTemperatureHistory();
		LOG.info("Temperature data purged for records before yesterday");
	}

}
