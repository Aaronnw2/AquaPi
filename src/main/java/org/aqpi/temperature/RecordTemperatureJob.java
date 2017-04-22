package org.aqpi.temperature;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class RecordTemperatureJob  implements Job {

	@Autowired
	private TemperatureRecordRepository temperatureRepository;
	
	private static final Logger LOG = LogManager.getLogger(RecordTemperatureJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		LOG.info("Recording temperature info");
		temperatureRepository.save(new TemperatureRecordEntity(71.0));
	}

}
