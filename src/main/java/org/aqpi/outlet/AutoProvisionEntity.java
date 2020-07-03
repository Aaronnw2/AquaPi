package org.aqpi.outlet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="AUTO_PROVISION")
public class AutoProvisionEntity {
	
	@Id
	@Column(name="PIN")
	private Integer pin;
	
	@Column(name="PIN_NAME")
	private String name;

	public AutoProvisionEntity() {}
	
	public AutoProvisionEntity(Integer inPin, String inName) {
		this.pin = inPin;
		this.name = inName;
	}
	
	public Integer getPin() {
		return pin;
	}

	public void setPin(Integer pin) {
		this.pin = pin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
