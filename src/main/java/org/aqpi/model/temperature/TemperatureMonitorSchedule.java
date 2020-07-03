package org.aqpi.model.temperature;

import java.util.Date;

public class TemperatureMonitorSchedule {

	private String cronExpression;
	private Date nextFireTime;
	
	public TemperatureMonitorSchedule(Date inNextTime, String inCron) {
		this.cronExpression = inCron;
		this.nextFireTime = inNextTime;
	}
	
	public TemperatureMonitorSchedule() {
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
