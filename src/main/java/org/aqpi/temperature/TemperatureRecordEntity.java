package org.aqpi.temperature;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="TEMPS")
public class TemperatureRecordEntity {

	@Id
	@GeneratedValue(strategy=IDENTITY)
	@Column(name="ID")
	private Long id;
	
	@Column(name="TIME")
	private Date time;
	
	@Column(name="TEMP")
	private Double temperature;
	
	public TemperatureRecordEntity(Double inTemperature) {
		this.time = new Date();
		this.temperature = inTemperature;
	}
	
	public TemperatureRecordEntity() {}
	
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
	public Double getTemperature() {
		return temperature;
	}
	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}
}
