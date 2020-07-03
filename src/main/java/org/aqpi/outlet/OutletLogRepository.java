package org.aqpi.outlet;

import java.util.Date;

import org.springframework.data.repository.CrudRepository;

public interface OutletLogRepository extends CrudRepository<OutletLogEntity, Long> {
	
	void deleteByTimeBefore(Date purgeDate);
}
