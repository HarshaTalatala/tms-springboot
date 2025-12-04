package com.harsha.tms.repository;

import com.harsha.tms.entity.Booking;
import com.harsha.tms.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByLoadId(UUID loadId);

    List<Booking> findByTransporterId(UUID transporterId);

    Optional<Booking> findByBidId(UUID bidId);

    List<Booking> findByStatus(BookingStatus status);

}

