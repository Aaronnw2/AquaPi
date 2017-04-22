package org.aqpi.outlet;

import org.springframework.data.repository.CrudRepository;

public interface AutoProvisionRepository extends CrudRepository<AutoProvisionEntity, Integer> {
	
	AutoProvisionEntity findByName(String name);
}
