package org.aqpi.temperature;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aqpi.api.model.exception.InternalErrorException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class RecordTemperatureJob  implements Job {

	private static final Logger LOG = LogManager.getLogger(RecordTemperatureJob.class);
	
	@Autowired
	private TemperatureMonitorDelegate delegate;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		LOG.info("Scheduled Temperature recording");
		try {
			delegate.recordNewTemperature();
		} catch (InternalErrorException e) {
			LOG.error("Error with scheduled temperature recording", e);
		}
	}

}
