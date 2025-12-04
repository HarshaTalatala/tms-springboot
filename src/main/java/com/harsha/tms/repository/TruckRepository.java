package com.harsha.tms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harsha.tms.entity.Truck;

@Repository
public interface TruckRepository extends JpaRepository<Truck, UUID> {

    List<Truck> findByTransporterTransporterId(UUID transporterId);

    List<Truck> findByTruckType(String truckType);

}

