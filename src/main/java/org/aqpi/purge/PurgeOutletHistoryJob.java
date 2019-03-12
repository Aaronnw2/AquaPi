package org.aqpi.purge;

import javax.transaction.Transactional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aqpi.outlet.OutletDelegate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class PurgeOutletHistoryJob implements Job {

	@Autowired
	private OutletDelegate delegate;
	
	private static final Logger LOG = LogManager.getLogger(PurgeOutletHistoryJob.class);
	
	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		delegate.purgeOldOutletHistory();
		LOG.info("Outlet history purged for records before yesterday");
	}

}
