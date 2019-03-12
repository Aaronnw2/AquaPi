package org.aqpi.temperature;

import java.util.Date;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

public interface TemperatureRecordRepository extends CrudRepository<TemperatureRecordEntity, Long>{
	@Modifying
	int deleteByTimeBefore(Date purgeAfterDate);
}
