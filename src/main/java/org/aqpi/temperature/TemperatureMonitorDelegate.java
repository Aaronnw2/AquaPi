package org.aqpi.temperature;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.quartz.CronScheduleBuilder.cronSchedule;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aqpi.model.temperature.TemperatureMonitorSchedule;
import org.aqpi.model.temperature.TemperatureRecord;
import org.aqpi.purge.PurgeOutletHistoryJob;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TemperatureMonitorDelegate {

	@Autowired
	private Scheduler scheduler;
	@Autowired
	private TemperatureRecordRepository temperatureRepository;
	
	private static final Logger LOG = LogManager.getLogger(TemperatureMonitorDelegate.class);
	
	public static final String TEMP_MONITOR_JOB_NAME = "temperatureMonitorJob";
	private static final String TEMP_TRIGGER_GROUP = "temperatureTriggerGroup";
	private static final String TEMP_JOB_GROUP = "temperatureJobGroup";
	private static final String TEMP_TRIGGER_NAME = "temperatureTrigger";
	
	private static final String HISTORY_PURGE_TRIGGER_GROUP = "tempPurgeTriggerGroup";
	private static final String HISTORY_PURGE_TRIGGER_NAME = "tempPurgeTrigger";
	private static final String HISTORY_PURGE_JOB_NAME = "tempPurgeJob";
	private static final String HISTORY_PURGE_JOB_GROUP = "tempPurgeJobGroup";
	private static final String ONCE_A_DAY_CRON_EXPRESSION = "0 0 12 * * ?";
	private static final int PURGE_HISTORY_AFTER_DAYS = 1;
	private static final String PURGE_JOB_DAY_KEY = "days";
	
	@PostConstruct
	private void maybeSetupHistoryPurge() throws SchedulerException {
		if (!scheduler.checkExists(new TriggerKey(HISTORY_PURGE_TRIGGER_NAME, HISTORY_PURGE_TRIGGER_GROUP))) {
			LOG.info("Creating default temperature purge job");
			JobDetail job = JobBuilder.newJob(PurgeOutletHistoryJob.class)
					.withIdentity(HISTORY_PURGE_JOB_NAME, HISTORY_PURGE_JOB_GROUP)
					.usingJobData(PURGE_JOB_DAY_KEY, PURGE_HISTORY_AFTER_DAYS)
					.build();
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(HISTORY_PURGE_TRIGGER_NAME, HISTORY_PURGE_TRIGGER_GROUP)
					.withSchedule(cronSchedule(ONCE_A_DAY_CRON_EXPRESSION))
					.forJob(job)
					.startNow()
					.build();
			scheduler.scheduleJob(job, trigger);
		}
	}
	
	public List<TemperatureRecord> getTemperatures() {
			return stream(temperatureRepository.findAll().spliterator(), false)
					.map(record -> new TemperatureRecord(record.getTime(), record.getTemperature()))
					.collect(toList());
	}
	
	public void setTemperatureMonitorSchedule(String cronExpression) throws SchedulerException {
		deleteTemperatureMonitorSchedule();
		JobDetail job = JobBuilder.newJob(RecordTemperatureJob.class)
				.withIdentity(TEMP_MONITOR_JOB_NAME, TEMP_JOB_GROUP)
				.build();
		Trigger newTrigger = TriggerBuilder.newTrigger()
				.withIdentity(TEMP_TRIGGER_NAME, TEMP_TRIGGER_GROUP)
				.withSchedule(cronSchedule(cronExpression))
				.forJob(job)
				.startNow()
				.build();
		LOG.info("Setting temperature monitor schedule to: " + cronExpression);
		scheduler.scheduleJob(job, newTrigger);
	}
	
	public TemperatureMonitorSchedule getTemperatureMonitoringSchedule() throws SchedulerException {
		return buildTemperatureSchedule(scheduler.getTriggersOfJob(new JobKey(TEMP_MONITOR_JOB_NAME, TEMP_JOB_GROUP)));
	}
	
	public void deleteTemperatureMonitorSchedule() throws SchedulerException {
		JobKey jobKey = new JobKey(TEMP_MONITOR_JOB_NAME, TEMP_JOB_GROUP);
		scheduler.unscheduleJob(new TriggerKey(TEMP_TRIGGER_NAME, TEMP_TRIGGER_GROUP));
		if (scheduler.checkExists(jobKey)) scheduler.deleteJob(jobKey);
		LOG.info("Removed temperature monitor schedule");
	}

	private TemperatureMonitorSchedule buildTemperatureSchedule(List<? extends Trigger> triggers) {
		if (triggers.isEmpty()) { return new TemperatureMonitorSchedule(); }
		CronTrigger trigger = (CronTrigger) triggers.get(0);
		return new TemperatureMonitorSchedule(trigger.getNextFireTime(), trigger.getCronExpression());
	}
}
