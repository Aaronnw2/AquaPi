package org.aqpi.temperature;

import java.util.Date;

import org.springframework.data.repository.CrudRepository;

public interface TemperatureRecordRepository extends CrudRepository<TemperatureRecordEntity, Long>{
	void deleteByTimeBefore(Date purgeAfterDate);
}
