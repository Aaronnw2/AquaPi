package org.aqpi.outlet;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="OUTLET_LOG")
public class OutletLogEntity {

	@Id
	@GeneratedValue(strategy=IDENTITY)
	@Column(name="ID")
	private Long id;
	
	@Column(name="TIME")
	private Date time;
	
	@Column(name="OUTLET")
	private String outlet;
	
	@Column(name="STATE")
	private String state;

	public OutletLogEntity() {}
	
	public OutletLogEntity(String outletName, String outletState) {
		this.time = new Date();
		this.outlet = outletName;
		this.state = outletState;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
