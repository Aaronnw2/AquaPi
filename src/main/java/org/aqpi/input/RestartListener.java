package org.aqpi.input;

import static com.pi4j.io.gpio.PinPullResistance.PULL_UP;
import static com.pi4j.io.gpio.PinState.LOW;
import static com.pi4j.io.gpio.RaspiPin.GPIO_27;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

@Service
public class RestartListener {
	
	private static final String RESTART_PIN_NAME = "RESTART";

	private static final Logger LOG = LoggerFactory.getLogger(RestartListener.class);
	
	@Autowired
	private GpioController gpioController;
	
	@PostConstruct
	public void setupRestartListener() {
		LOG.info("Setting up restart button listener...");	
		final GpioPinDigitalInput restartButton = gpioController.provisionDigitalInputPin(GPIO_27, RESTART_PIN_NAME, PULL_UP);
		restartButton.setDebounce(1000);
		restartButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				GpioPin pin = event.getPin();
				LOG.info(String.format("PIN STATE CHANGE: %s, Edge: (%s, %s), Pin: %s, State: %s", event.getEventType(), event.getEdge().getName(),
						event.getEdge().getValue(), pin, event.getState()));
				if (pin.getName().equals(RESTART_PIN_NAME) && event.getState().equals(LOW)) {
					LOG.info("Detected input Low");
					try {
						SECONDS.sleep(2);
						if (restartButton.isLow()) {
							LOG.info("=================================== Restart Called, shutting down ===================================");
							Runtime.getRuntime().exec("sudo reboot");
						} else {
							LOG.info("Possible voltage dip, skipping restart");
						}
					}
					catch (IOException | InterruptedException e) { LOG.error("Error calling restart!", e); }
				}
			}
		});
		LOG.info("Restart listener set!");
	}

}
