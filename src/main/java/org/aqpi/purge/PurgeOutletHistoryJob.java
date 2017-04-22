package org.aqpi.purge;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aqpi.outlet.OutletLogRepository;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class PurgeOutletHistoryJob implements Job {

	@Autowired
	private OutletLogRepository logRepository;
	
	private static final Logger LOG = LogManager.getLogger(PurgeOutletHistoryJob.class);
	
	private static final String PURGE_JOB_DAY_KEY = "days";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Integer days = dataMap.getIntegerFromString(PURGE_JOB_DAY_KEY);
		LocalDate purgeDate = LocalDate.now().minusDays(days);
		logRepository.deleteByTimeBefore(Date.from(purgeDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		LOG.info("Outlet history purged for records before " + purgeDate.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
	}

}
