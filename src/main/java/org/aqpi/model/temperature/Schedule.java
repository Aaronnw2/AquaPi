package org.aqpi.model.temperature;

import java.util.Date;

public class Schedule {

	private String cronExpression;
	private Date nextFireTime;
	
	public Schedule(Date inNextTime, String inCron) {
		this.cronExpression = inCron;
		this.nextFireTime = inNextTime;
	}
	
	public Schedule() {
		this.cronExpression = "none";
		this.nextFireTime = null;
	}
	
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	public Date getNextFireTime() {
		return nextFireTime;
	}
	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	
}
