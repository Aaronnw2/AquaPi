package org.aqpi.api.model.outlet;

import java.util.Date;

public class OutletLog {

	private Date time;
	
	private String outlet;
	
	private String state;

	public OutletLog(Date inTime, String inOutlet, String inState) {
		this.time = inTime;
		this.outlet = inOutlet;
		this.state = inState;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getOutlet() {
		return outlet;
	}

	public void setOutlet(String outlet) {
		this.outlet = outlet;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
