package com.harsha.tms.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harsha.tms.entity.Bid;
import com.harsha.tms.entity.BidStatus;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByLoad_Id(UUID loadId);

    List<Bid> findByTransporter_TransporterId(UUID transporterId);

    List<Bid> findByStatus(BidStatus status);

    List<Bid> findByLoad_IdAndStatus(UUID loadId, BidStatus status);

    List<Bid> findByTransporter_TransporterIdAndStatus(UUID transporterId, BidStatus status);

    List<Bid> findByLoad_IdAndTransporter_TransporterId(UUID loadId, UUID transporterId);

    List<Bid> findByLoad_IdAndTransporter_TransporterIdAndStatus(UUID loadId, UUID transporterId, BidStatus status);
}

