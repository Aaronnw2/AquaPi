package org.aqpi.api.model.schedule;

import org.aqpi.api.model.OutletState;

public class NewTriggerRequest {
	
	private String cronExpression;
	private OutletState state;
	private String outletName;

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public OutletState getState() {
		return state;
	}

	public void setState(OutletState state) {
		this.state = state;
	}

	public String getOutletName() {
		return outletName;
	}

	public void setOutletName(String outletName) {
		this.outletName = outletName;
	}
}
