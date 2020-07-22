package org.aqpi.temperature;

import static java.nio.file.Files.readAllLines;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.quartz.CronScheduleBuilder.cronSchedule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.aqpi.api.model.exception.InternalErrorException;
import org.aqpi.model.temperature.Schedule;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Transactional
public class TemperatureMonitorDelegate {

	@Autowired
	private Scheduler scheduler;
	@Autowired
	private TemperatureRecordRepository temperatureRepository;

	private static final Logger LOG = LoggerFactory.getLogger(TemperatureMonitorDelegate.class);

	public static final String TEMP_MONITOR_JOB_NAME = "temperatureMonitorJob";
	private static final String TEMP_TRIGGER_GROUP = "temperatureTriggerGroup";
	private static final String TEMP_JOB_GROUP = "temperatureJobGroup";
	private static final String TEMP_TRIGGER_NAME = "temperatureTrigger";

	private static final String HISTORY_PURGE_TRIGGER_GROUP = "tempPurgeTriggerGroup";
	private static final String HISTORY_PURGE_TRIGGER_NAME = "tempPurgeTrigger";
	private static final String HISTORY_PURGE_JOB_NAME = "tempPurgeJob";
	private static final String HISTORY_PURGE_JOB_GROUP = "tempPurgeJobGroup";
	private static final String ONCE_A_DAY_CRON_EXPRESSION = "0 0 0 * * ?";
	private static final String SENSOR_FILE = "/sys/bus/w1/devices/28-000006d83d20/w1_slave";

	@PostConstruct
	private void maybeSetupHistoryPurge() throws SchedulerException {
		if (!scheduler.checkExists(new TriggerKey(HISTORY_PURGE_TRIGGER_NAME, HISTORY_PURGE_TRIGGER_GROUP))) {
			LOG.info("Creating default temperature purge job");
			JobDetail job = JobBuilder.newJob(PurgeOutletHistoryJob.class)
					.withIdentity(HISTORY_PURGE_JOB_NAME, HISTORY_PURGE_JOB_GROUP)
					.build();
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(HISTORY_PURGE_TRIGGER_NAME, HISTORY_PURGE_TRIGGER_GROUP)
					.withSchedule(cronSchedule(ONCE_A_DAY_CRON_EXPRESSION))
					.forJob(job)
					.startAt(fiveMinutesInTheFuture())
					.build();
			scheduler.scheduleJob(job, trigger);
		}
	}

	public List<TemperatureRecord> getTemperatures() {
		return stream(temperatureRepository.findAll().spliterator(), false)
				.map(record -> new TemperatureRecord(record.getTime(), record.getTemperature()))
				.collect(toList());
	}
	
	public TemperatureRecord getLatestTemperature() {
		TemperatureRecordEntity temperature = temperatureRepository.findFirstByOrderByTimeDesc();
		return new TemperatureRecord(temperature.getTime(), temperature.getTemperature());
	}

	public TemperatureRecord recordNewTemperature() throws InternalErrorException {
		LOG.info("Recording temperature info");
		if (!Files.exists(Paths.get(SENSOR_FILE))) {
			throw new InternalErrorException("Could Not record temperature, 1-wire device doesn't exist!");
		}
		try {
			String tempLine = readAllLines(Paths.get(SENSOR_FILE)).get(1);
			Double rawTemp = Double.parseDouble(tempLine.substring(tempLine.indexOf("t=") + 2));
			Double temp = Math.round((((rawTemp/1000) * (9/5.0)) + 32) * 1000) / 1000D;
			temperatureRepository.save(new TemperatureRecordEntity(temp));
			return new TemperatureRecord(new Date(), temp);
		} catch (IOException e) { throw new InternalErrorException("Error reading from 1-wire sensor", e); }
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

	public Schedule getTemperatureMonitoringSchedule() throws SchedulerException {
		return buildTemperatureSchedule(scheduler.getTriggersOfJob(new JobKey(TEMP_MONITOR_JOB_NAME, TEMP_JOB_GROUP)));
	}

	public void deleteTemperatureMonitorSchedule() throws SchedulerException {
		JobKey jobKey = new JobKey(TEMP_MONITOR_JOB_NAME, TEMP_JOB_GROUP);
		scheduler.unscheduleJob(new TriggerKey(TEMP_TRIGGER_NAME, TEMP_TRIGGER_GROUP));
		if (scheduler.checkExists(jobKey)) scheduler.deleteJob(jobKey);
		LOG.info("Removed temperature monitor schedule");
	}

	public void deleteTemperatureHistory() {
		temperatureRepository.deleteAll();
	}

	public void purgeOldTemperatureHistory() {
		LocalDateTime purgeDate = LocalDateTime.now().minusDays(1);
		int rowsDeleted = temperatureRepository.deleteByTimeBefore(Date.from(purgeDate.atZone(ZoneId.systemDefault()).toInstant()));
		LOG.info("Removed " + rowsDeleted + " temperature records");
	}
	
	public void deleteTemperaturePurgeSchedule() throws SchedulerException {
		TriggerKey triggerKey = new TriggerKey(HISTORY_PURGE_TRIGGER_NAME, HISTORY_PURGE_TRIGGER_GROUP);
		if (scheduler.checkExists(triggerKey)) {
			scheduler.unscheduleJob(triggerKey);
			scheduler.deleteJob(new JobKey(HISTORY_PURGE_JOB_NAME, HISTORY_PURGE_JOB_GROUP));
		}
	}
	
	private Schedule buildTemperatureSchedule(List<? extends Trigger> triggers) {
		if (triggers.isEmpty()) { return new Schedule(); }
		CronTrigger trigger = (CronTrigger) triggers.get(0);
		return new Schedule(trigger.getNextFireTime(), trigger.getCronExpression());
	}
	
	private Date fiveMinutesInTheFuture() {
		Calendar cal = Calendar.getInstance();
	    cal.setTime(new Date());
	    cal.add(Calendar.MINUTE, 5);
	    return cal.getTime();
	}
}