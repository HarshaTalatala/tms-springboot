package com.harsha.tms.repository;

import com.harsha.tms.entity.Load;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoadRepository extends JpaRepository<Load, UUID> {

}

