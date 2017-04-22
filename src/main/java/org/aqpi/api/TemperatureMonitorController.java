package org.aqpi.api;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.aqpi.api.model.exception.BadRequestException;
import org.aqpi.model.temperature.TemperatureMonitorSchedule;
import org.aqpi.model.temperature.TemperatureRecord;
import org.aqpi.temperature.TemperatureMonitorDelegate;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TemperatureMonitorController {

	@Autowired
	private TemperatureMonitorDelegate delegate;
	
	@RequestMapping(path="/temps", method=GET)
	public ResponseEntity<List<TemperatureRecord>> getTemperatures() throws SchedulerException {
		return new ResponseEntity<List<TemperatureRecord>>(delegate.getTemperatures(), HttpStatus.OK);
	}
	
	@RequestMapping(path="/temps/schedule", method=GET)
	public ResponseEntity<TemperatureMonitorSchedule> getSchedule() throws SchedulerException {
		return new ResponseEntity<TemperatureMonitorSchedule>(delegate.getTemperatureMonitoringSchedule(), HttpStatus.OK);
	}
	
	@RequestMapping(path="/temps/schedule", method=POST)
	public ResponseEntity<Void> setSchedule(@RequestParam("cron") String cron) throws BadRequestException, SchedulerException {
		if (cron == null || cron.equals("")) { throw new BadRequestException("Invalid cron expression: " + cron); }
		delegate.setTemperatureMonitorSchedule(cron);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(path="/temps/schedule", method=DELETE)
	public ResponseEntity<Void> deleteSchedule() throws SchedulerException {
		delegate.deleteTemperatureMonitorSchedule();
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}