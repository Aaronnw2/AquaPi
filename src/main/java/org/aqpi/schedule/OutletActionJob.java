package org.aqpi.schedule;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aqpi.api.model.OutletState;
import org.aqpi.api.model.exception.BadRequestException;
import org.aqpi.outlet.OutletDelegate;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class OutletActionJob implements Job {

	@Autowired
	private OutletDelegate outletDelegate;

	private static final Logger LOG = LogManager.getLogger(OutletActionJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			String outletName = dataMap.getString("outlet");
			OutletState state = (OutletState) dataMap.get("state");
			outletDelegate.setOutletToState(outletName, state);
		} catch (BadRequestException e) {
			LOG.error("Error running outlet state change job", e);
		}
	}

}
