package org.aqpi.api;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.aqpi.api.feeder.FeederDelegate;
import org.aqpi.api.feeder.FeederRecord;
import org.aqpi.api.model.exception.BadRequestException;
import org.aqpi.model.temperature.Schedule;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeederController {
	
	@Autowired
	private FeederDelegate delegate;
	
	@RequestMapping(path="/feeder/schedule", method=GET)
	public ResponseEntity<Schedule> getFeederSchedule() throws SchedulerException {
		return new ResponseEntity<Schedule>(delegate.getFeederSchedule(), HttpStatus.OK);
	}
	
	@RequestMapping(path="/feeder/schedule", method=POST)
	public ResponseEntity<Void> setSchedule(@RequestParam("cron") String cron) throws BadRequestException, SchedulerException {
		if (cron == null || cron.equals("")) { throw new BadRequestException("Invalid cron expression: " + cron); }
		delegate.setFeederSchedule(cron);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(path="/feeder", method=GET)
	public ResponseEntity<FeederRecord> getLastFeederTime() throws SchedulerException {
		return new ResponseEntity<FeederRecord>(delegate.getLastFeedTime(), HttpStatus.OK);
	}
	
	@RequestMapping(path="/feeder", method=POST)
	public ResponseEntity<Void> feed() throws SchedulerException, InterruptedException {
		delegate.feedNow();
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

}
