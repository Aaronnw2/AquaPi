package org.aqpi.api;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.aqpi.api.model.exception.BadRequestException;
import org.aqpi.api.model.exception.NotFoundException;
import org.aqpi.api.model.schedule.NewTriggerRequest;
import org.aqpi.api.model.schedule.TriggerInformation;
import org.aqpi.schedule.SchedulerDelegate;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {

	@Autowired
	private SchedulerDelegate delegate;
	
	@RequestMapping(path="/triggers", method=GET)
	public ResponseEntity<List<TriggerInformation>> getTriggers() throws SchedulerException {
		return new ResponseEntity<List<TriggerInformation>>(delegate.getTriggers(), HttpStatus.OK);
	}
	
	@RequestMapping(path="/triggers", method=POST)
	public ResponseEntity<Void> addTrigger(@RequestBody NewTriggerRequest newTrigger) throws SchedulerException, BadRequestException {
		delegate.addTrigger(newTrigger);
		return new ResponseEntity<Void>(OK);
	}
	
	@RequestMapping(path="/triggers/{name}", method=RequestMethod.DELETE)
	public ResponseEntity<Void> deleteTrigger(@PathVariable("name") String triggerName) throws SchedulerException, NotFoundException {
		delegate.deleteTrigger(triggerName);
		return new ResponseEntity<Void>(OK);
	}
}
