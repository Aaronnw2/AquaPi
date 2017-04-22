package org.aqpi.model.temperature;

import java.util.Date;

public class TemperatureRecord {

	private Date time;
	private Double temperature;
	
	public TemperatureRecord(Date inTime, Double inTemperature) {
		this.time = inTime;
		this.temperature = inTemperature;
	}
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public Double getTemperature() {
		return temperature;
	}
	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}	
}
