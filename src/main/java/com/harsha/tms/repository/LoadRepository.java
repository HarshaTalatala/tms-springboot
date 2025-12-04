package com.harsha.tms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.Load;

@Repository
public interface LoadRepository extends JpaRepository<Load, UUID> {

    List<Load> findByShipperId(UUID shipperId);

    List<Load> findByStatus(BookingStatus status);

    List<Load> findByShipperIdAndStatus(UUID shipperId, BookingStatus status);
}

