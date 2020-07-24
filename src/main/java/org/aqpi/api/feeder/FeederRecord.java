package org.aqpi.api.feeder;

import java.util.Date;

public class FeederRecord {
	private Date lastFeedTime;
	private Date nextFeedTime;
	
	public Date getLastFeedTime() {
		return lastFeedTime;
	}
	public void setLastFeedTime(Date lastFeedTime) {
		this.lastFeedTime = lastFeedTime;
	}
	public Date getNextFeedTime() {
		return nextFeedTime;
	}
	public void setNextFeedTime(Date nextFeedTime) {
		this.nextFeedTime = nextFeedTime;
	}

}
