package org.aqpi.api;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;

import org.aqpi.api.model.OutletState;
import org.aqpi.api.model.exception.BadRequestException;
import org.aqpi.api.model.exception.NotFoundException;
import org.aqpi.api.model.outlet.NewOutletRequest;
import org.aqpi.api.model.outlet.OutletInformation;
import org.aqpi.api.model.outlet.OutletLog;
import org.aqpi.outlet.OutletDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OutletController {

	@Autowired
	private OutletDelegate delegate;
	
	@RequestMapping(path="/outlets", method=GET)
	public ResponseEntity<List<OutletInformation>> getOutlets() {
		return new ResponseEntity<List<OutletInformation>>(delegate.getOutlets(), HttpStatus.OK);
	}
	
	@RequestMapping(path="/outlets", method=POST)
	public ResponseEntity<Void> addOutlet(@RequestBody NewOutletRequest request) {
		delegate.addNewOutlet(request.getName(), request.getPin());
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(path="/outlets/{name}/{state}", method=PUT)
	public ResponseEntity<Void> setOutletToState(@PathVariable("name") String name,
			@PathVariable("state")String state) throws BadRequestException {
		delegate.setOutletToState(name, OutletState.getByValue(state));
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(path="/outlets/{name}", method=DELETE)
	public ResponseEntity<Void> removeOutlet(@PathVariable("name") String name) throws NotFoundException {
		delegate.removeOutlet(name);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(path="/outlets/history", method=GET)
	public ResponseEntity<List<OutletLog>> getOutletLog() {
		return new ResponseEntity<List<OutletLog>>(delegate.getOutletLog(), HttpStatus.OK);
	}
	
	@RequestMapping(path="/outlets/history/old", method=DELETE)
	public ResponseEntity<List<OutletLog>> purgeRecentOutletLog() {
		delegate.purgeOldOutletHistory();
		return new ResponseEntity<List<OutletLog>>(HttpStatus.OK);
	}
	
	@RequestMapping(path="/outlets/history/all", method=DELETE)
	public ResponseEntity<List<OutletLog>> purgeAllOutletLog() {
		delegate.purgeAllOutletHistory();
		return new ResponseEntity<List<OutletLog>>(HttpStatus.OK);
	}
}
