package com.microservice.authenticationservice.repository;

import java.util.Optional;

import com.microservice.authenticationservice.models.ERole;
import com.microservice.authenticationservice.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
