package org.aqpi.purge;

import javax.transaction.Transactional;

import org.aqpi.outlet.OutletDelegate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PurgeOutletHistoryJob implements Job {

	@Autowired
	private OutletDelegate delegate;
	
	private static final Logger LOG = LoggerFactory.getLogger(PurgeOutletHistoryJob.class);
	
	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		delegate.purgeOldOutletHistory();
		LOG.info("Outlet history purged for records before yesterday");
	}

}
