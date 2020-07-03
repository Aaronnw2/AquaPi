package org.aqpi.api.model.outlet;

import org.aqpi.api.model.OutletState;

public class OutletInformation {

	private String outletName;
	private OutletState state;

	public OutletInformation(String inOutletName, OutletState inState) {
		this.outletName = inOutletName;
		this.state = inState;
	}
	
	public String getOutletName() {
		return outletName;
	}

	public void setOutletName(String pinName) {
		this.outletName = pinName;
	}

	public OutletState getState() {
		return state;
	}

	public void setState(OutletState state) {
		this.state = state;
	}
}
