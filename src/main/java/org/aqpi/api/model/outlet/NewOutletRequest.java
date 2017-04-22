package org.aqpi.api.model.outlet;

public class NewOutletRequest {

	private String name;
	private int pin;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPin() {
		return pin;
	}
	public void setPin(int pin) {
		this.pin = pin;
	}
}
