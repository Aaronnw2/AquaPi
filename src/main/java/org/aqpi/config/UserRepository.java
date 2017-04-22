package org.aqpi.config;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {
	
	public UserEntity findByUserName(String username);
	
}
