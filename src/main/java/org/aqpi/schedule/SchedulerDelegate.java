package org.aqpi.schedule;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aqpi.api.model.OutletState;
import org.aqpi.api.model.exception.BadRequestException;
import org.aqpi.api.model.exception.NotFoundException;
import org.aqpi.api.model.outlet.OutletInformation;
import org.aqpi.api.model.schedule.NewTriggerRequest;
import org.aqpi.api.model.schedule.TriggerInformation;
import org.aqpi.outlet.OutletDelegate;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
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
import org.springframework.stereotype.Service;

@Service
public class SchedulerDelegate {

	@Autowired
	private Scheduler scheduler;
	@Autowired
	private OutletDelegate outletDelegate;
	
	private static final Logger LOG = LoggerFactory.getLogger(SchedulerDelegate.class);
	private static final String OUTLET_KEY = "outlet";
	private static final String STATE_KEY = "state";
	private static final String OUTLET_JOB_GROUP = "outletJobGroup";
	private static final String OUTLET_TRIGGER_GROUP = "outletTriggerGroup";

	public List<TriggerInformation> getTriggers() throws SchedulerException {
		return scheduler.getJobKeys(jobGroupEquals(OUTLET_JOB_GROUP)).stream()
				.flatMap(jobKey -> getTriggersFromJob(jobKey))
				.map(trigger -> buildTriggerResponse(trigger))
				.collect(Collectors.toList());
	}

	public String addTrigger(NewTriggerRequest triggerRequest) throws SchedulerException, BadRequestException {
		if (!outletDelegate.isOutletProvisioned(triggerRequest.getOutletName())) {
			throw new BadRequestException("Outlet doesn't exist:" + triggerRequest.getOutletName());
		}
		String name = UUID.randomUUID().toString();
		JobDetail job = newJob(OutletActionJob.class)
				.withIdentity(name + "-job", OUTLET_JOB_GROUP)
				.usingJobData(buildJobData(triggerRequest))
				.build();
		Trigger newTrigger = TriggerBuilder.newTrigger()
				.withIdentity(name, OUTLET_TRIGGER_GROUP)
				.withSchedule(cronSchedule(triggerRequest.getCronExpression()))
				.forJob(job)
				.startNow()
				.build();
		scheduler.scheduleJob(job, newTrigger);
		return name;
	}

	public void deleteTrigger(String triggerName) throws SchedulerException, NotFoundException {
		Trigger trigger = scheduler.getTrigger(new TriggerKey(triggerName, OUTLET_TRIGGER_GROUP));
		if (trigger == null) { throw new NotFoundException("Invalid trigger: " + triggerName); }
		String jobName = trigger.getJobKey().getName();
		scheduler.unscheduleJob(new TriggerKey(triggerName, OUTLET_TRIGGER_GROUP));
		scheduler.deleteJob(new JobKey(jobName, OUTLET_JOB_GROUP));
	}

	private JobDataMap buildJobData(NewTriggerRequest triggerRequest) {
		JobDataMap jobParameters = new JobDataMap();
		jobParameters.put(OUTLET_KEY, triggerRequest.getOutletName());
		jobParameters.put(STATE_KEY, triggerRequest.getState());
		return jobParameters;
	}

	private Stream<? extends Trigger> getTriggersFromJob(JobKey jobKey) {
		try {
			return scheduler.getTriggersOfJob(jobKey).stream();
		} catch (SchedulerException e) {
			LOG.error("Error getting job triggers", e);
			return null;
		}
	}

	private TriggerInformation buildTriggerResponse(Trigger trigger) {
		try {
			TriggerInformation info = new TriggerInformation();
			CronTrigger cronTrigger = (CronTrigger)trigger;
			info.setCronExpression(cronTrigger.getCronExpression());
			info.setNextFireTime(cronTrigger.getNextFireTime());
			info.setTriggerName(cronTrigger.getKey().getName());
			JobDetail jobInfo = scheduler.getJobDetail(trigger.getJobKey());
			info.setJobName(jobInfo.getKey().getName());
			JobDataMap jobData = jobInfo.getJobDataMap();
			info.setOutlet(new OutletInformation(jobData.getString(OUTLET_KEY), 
					(OutletState) jobData.get(STATE_KEY)));
			return info;
		} catch (SchedulerException e) {
			LOG.error("Error building trigger information", e);
		}
		return null;
	}
}
