package org.aqpi.api.model.schedule;

import java.util.Date;

import org.aqpi.api.model.outlet.OutletInformation;

public class TriggerInformation {
	
	private String cronExpression;
	private String jobName;
	private String triggerName;
	private Date nextFireTime;
	private OutletInformation outlet;
	
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getTriggerName() {
		return triggerName;
	}
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}
	public Date getNextFireTime() {
		return nextFireTime;
	}
	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	public OutletInformation getOutlet() {
		return outlet;
	}
	public void setOutlet(OutletInformation outlet) {
		this.outlet = outlet;
	}
}
