package org.aqpi.api.feeder;

import static com.pi4j.io.gpio.PinState.HIGH;
import static com.pi4j.io.gpio.PinState.LOW;
import static com.pi4j.io.gpio.RaspiPin.getPinByAddress;
import static org.quartz.CronScheduleBuilder.cronSchedule;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.aqpi.model.temperature.Schedule;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

@Service
@Transactional
public class FeederDelegate {

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private GpioController controller;
	
	@Value("${feeder.pin:24}")
	private Integer feederPin;
	
	private static final Logger LOG = LoggerFactory.getLogger(FeederDelegate.class);
	public static final String FEEDER_PIN_NAME = "Feeder Pin";
	private static final String FEEDER_TRIGGER_NAME = "feederTrigger";
	private static final String FEEDER_TRIGGER_GROUP = "feederGroup";
	private static final String ONCE_A_DAY_CRON_EXPRESSION = "0 0 20 * * ?	";
	private static final String FEEDER_JOB_NAME = "feederJob";
	private static final String FEEDER_JOB_GROUP = "feederJobGroup";
	
	@PostConstruct
	private void setup() throws SchedulerException {
		controller.provisionDigitalOutputPin(getPinByAddress(this.feederPin), FEEDER_PIN_NAME, LOW);
		if (!this.scheduler.checkExists(new TriggerKey(FEEDER_TRIGGER_NAME, FEEDER_TRIGGER_GROUP))) {
			LOG.info("Creating default feeder job");
			JobDetail job = JobBuilder.newJob(FeederJob.class)
					.withIdentity(FEEDER_TRIGGER_NAME, FEEDER_TRIGGER_GROUP)
					.build();
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(FEEDER_TRIGGER_NAME, FEEDER_TRIGGER_GROUP)
					.withSchedule(cronSchedule(ONCE_A_DAY_CRON_EXPRESSION))
					.forJob(job)
					.startAt(buildStartTime())
					.build();
			scheduler.scheduleJob(job, trigger);
		}	
	}
	
	private Date buildStartTime() {
		return new Date(LocalDate.now()
				.plusDays(1)
				.atStartOfDay()
				.plusHours(12)
				.toEpochSecond(ZoneOffset.UTC));
	}

	public Schedule getFeederSchedule() throws SchedulerException {
		return buildFeederSchedule(scheduler.getTriggersOfJob(new JobKey(FEEDER_JOB_NAME, FEEDER_JOB_GROUP)));
	}

	private Schedule buildFeederSchedule(List<? extends Trigger> triggers) {
		if (triggers.isEmpty()) { return new Schedule(); }
		CronTrigger trigger = (CronTrigger) triggers.get(0);
		return new Schedule(trigger.getNextFireTime(), trigger.getCronExpression());
	}

	public void setFeederSchedule(String cron) throws SchedulerException {
		deleteFeederSchedule();
		JobDetail job = JobBuilder.newJob(FeederJob.class)
				.withIdentity(FEEDER_JOB_NAME, FEEDER_JOB_GROUP)
				.build();
		Trigger newTrigger = TriggerBuilder.newTrigger()
				.withIdentity(FEEDER_TRIGGER_NAME, FEEDER_TRIGGER_GROUP)
				.withSchedule(cronSchedule(cron))
				.forJob(job)
				.startNow()
				.build();
		LOG.info("Setting Feeder schedule to: " + cron);
		scheduler.scheduleJob(job, newTrigger);
	}

	private void deleteFeederSchedule() throws SchedulerException {
		JobKey jobKey = new JobKey(FEEDER_JOB_NAME, FEEDER_JOB_GROUP);
		scheduler.unscheduleJob(new TriggerKey(FEEDER_TRIGGER_NAME, FEEDER_TRIGGER_GROUP));
		if (scheduler.checkExists(jobKey)) scheduler.deleteJob(jobKey);
		LOG.info("Removed feeder schedule");
	}

	public FeederRecord getLastFeedTime() throws SchedulerException {
		FeederRecord feederRecord = new FeederRecord();
		Trigger trigger = scheduler.getTriggersOfJob(new JobKey(FEEDER_JOB_NAME, FEEDER_JOB_GROUP))
				.get(0);
		feederRecord.setLastFeedTime(trigger.getPreviousFireTime());
		feederRecord.setNextFeedTime(trigger.getNextFireTime());
		return feederRecord;
	}

	public void feedNow() throws InterruptedException {
		this.controller.setState(HIGH, (GpioPinDigitalOutput) this.controller.getProvisionedPin(FEEDER_PIN_NAME));
		TimeUnit.SECONDS.sleep(1);
		this.controller.setState(LOW, (GpioPinDigitalOutput) this.controller.getProvisionedPin(FEEDER_PIN_NAME));
		LOG.info("Feeding complete");
	}

}
