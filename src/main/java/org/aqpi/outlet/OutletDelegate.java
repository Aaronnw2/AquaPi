package org.aqpi.outlet;

import static com.pi4j.io.gpio.PinMode.DIGITAL_OUTPUT;
import static com.pi4j.io.gpio.PinState.HIGH;
import static com.pi4j.io.gpio.PinState.LOW;
import static com.pi4j.io.gpio.RaspiPin.getPinByAddress;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.quartz.CronScheduleBuilder.cronSchedule;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aqpi.api.model.OutletState;
import org.aqpi.api.model.exception.BadRequestException;
import org.aqpi.api.model.exception.NotFoundException;
import org.aqpi.api.model.outlet.OutletInformation;
import org.aqpi.api.model.outlet.OutletLog;
import org.aqpi.purge.PurgeOutletHistoryJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

@Service
@Transactional
public class OutletDelegate {

	@Autowired
	private GpioController controller;
	@Autowired
	private AutoProvisionRepository pinRepository;
	@Autowired
	private OutletLogRepository logRepository;
	@Autowired
	private Scheduler scheduler;
	
	private static final Logger LOG  = LogManager.getLogger(OutletDelegate.class);
	
	private static final String HISTORY_PURGE_TRIGGER_GROUP = "historyPurgeTriggerGroup";
	private static final String HISTORY_PURGE_TRIGGER_NAME = "historyPurgeTrigger";
	private static final String HISTORY_PURGE_JOB_NAME = "historyPurgeJob";
	private static final String HISTORY_PURGE_JOB_GROUP = "historyPurgeJobGroup";
	private static final String ONCE_A_DAY_CRON_EXPRESSION = "0 0 12 * * ?";
	private static final int PURGE_HISTORY_AFTER_DAYS = 14;
	private static final String PURGE_JOB_DAY_KEY = "days";
	
	@PostConstruct
	private void initializePins() throws SchedulerException {
		pinRepository.findAll().forEach(
				pinEntity -> controller.provisionDigitalOutputPin(getPinByAddress(pinEntity.getPin()),
						pinEntity.getName(), LOW));
		maybeSetupPurgeLogJob();
	}

	public List<OutletInformation> getOutlets() {
		return controller.getProvisionedPins().stream()
				.filter(pin -> pin.getMode().equals(DIGITAL_OUTPUT))
				.map(pin -> buildOutletInformation((GpioPinDigitalOutput)pin))
				.collect(toList());
	}
	
	public void setOutletToState(String outletName, OutletState state) throws BadRequestException {
		if (controller.getProvisionedPin(outletName) == null) { throw new BadRequestException("Invalid outlet: " + outletName); }
		LOG.info("Setting outlet: " + outletName + " to: " + state);
		controller.setState(StateMap.getPinState(state),
				(GpioPinDigitalOutput)controller.getProvisionedPin(outletName));
		logRepository.save(new OutletLogEntity(outletName, state.getValue()));
	}
	
	public void addNewOutlet(String name, int pin) {
		LOG.info("Adding outlet: " + name + " for pin: " + pin);
		controller.provisionDigitalOutputPin(getPinByAddress(pin), name, LOW);
		pinRepository.save(new AutoProvisionEntity(pin, name));
	}
	
	public void removeOutlet(String name) throws NotFoundException {
		AutoProvisionEntity entity = pinRepository.findByName(name);
		if (entity == null) { throw new NotFoundException("No outlet named: " + name); }
		LOG.info("Removing outlet: " + name);
		controller.unprovisionPin(controller.getProvisionedPin(name));
		pinRepository.delete(entity);
	}
	
	public List<OutletLog> getOutletLog() {
		return stream(logRepository.findAll().spliterator(), false)
				.map(entity -> buildOutletLog(entity))
				.collect(toList());
	}
	
	public void purgeOldOutletHistory() {
		LocalDate purgeDate = LocalDate.now().minusDays(1);
		logRepository.deleteByTimeBefore(Date.from(purgeDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
	}
	
	public void purgeAllOutletHistory() {
		logRepository.deleteAll();
	}

	public Boolean isOutletProvisioned(String outletName) {
		List<String> names = getOutlets()
				.stream()
				.map(outlet -> outlet.getOutletName())
				.collect(toList());
		return names.contains(outletName);
	}
	
	private OutletLog buildOutletLog(OutletLogEntity entity) {
		return new OutletLog(entity.getTime(), entity.getOutlet(), entity.getState());
	}
	
	private OutletInformation buildOutletInformation(GpioPinDigitalOutput pin) {
		return new OutletInformation(pin.getName(), StateMap.getOutletState(pin.getState()));
	}
	
	private void maybeSetupPurgeLogJob() throws SchedulerException {
		if (!scheduler.checkExists(new TriggerKey(HISTORY_PURGE_TRIGGER_NAME, HISTORY_PURGE_TRIGGER_GROUP))) {
			LOG.info("Creating default outlet historty purge job");
			JobDetail job = JobBuilder.newJob(PurgeOutletHistoryJob.class)
					.withIdentity(HISTORY_PURGE_JOB_NAME, HISTORY_PURGE_JOB_GROUP)
					.usingJobData(PURGE_JOB_DAY_KEY, PURGE_HISTORY_AFTER_DAYS)
					.build();
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(HISTORY_PURGE_TRIGGER_NAME, HISTORY_PURGE_TRIGGER_GROUP)
					.withSchedule(cronSchedule(ONCE_A_DAY_CRON_EXPRESSION))
					.forJob(job)
					.startNow()
					.build();
			scheduler.scheduleJob(job, trigger);
		}
	}

	private enum StateMap {
		ON(OutletState.ON, HIGH),
		OFF(OutletState.OFF, LOW);
		
		private OutletState outletState;
		private PinState pinState;
		
		private StateMap(OutletState inOutletState, PinState inPinState) {
			this.outletState = inOutletState;
			this.pinState = inPinState;
		}
		
		public static PinState getPinState(OutletState outletState) {
			if (outletState.equals(OutletState.ON)) { return ON.pinState; }
			if (outletState.equals(OutletState.OFF)) { return OFF.pinState; }
			return null;
		}
		
		public static OutletState getOutletState(PinState inPinState) {
			if (inPinState.equals(HIGH)) { return ON.outletState; }
			if (inPinState.equals(LOW)) { return OFF.outletState; }
			return null;
		}
	}
}
