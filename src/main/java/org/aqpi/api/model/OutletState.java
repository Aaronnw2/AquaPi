package org.aqpi.api.model;

import org.aqpi.api.model.exception.BadRequestException;

public enum OutletState {

	ON("ON"),
	OFF("OFF");
	
	private String value;
	
	private OutletState(String inValue) {
		this.value = inValue;
	}
	
	public String getValue() {
		return value;
	}
	
	public static OutletState getByValue(String inValue) throws BadRequestException {
		if (inValue.equalsIgnoreCase(ON.getValue())) { return ON; }
		if (inValue.equalsIgnoreCase(OFF.getValue())) { return OFF; }
		throw new BadRequestException("Invalid state: " + inValue);
	}
}
