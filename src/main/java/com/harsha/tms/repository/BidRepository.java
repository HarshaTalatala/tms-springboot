package com.harsha.tms.repository;

import com.harsha.tms.entity.Bid;
import com.harsha.tms.entity.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByLoadId(UUID loadId);

    List<Bid> findByTransporterId(UUID transporterId);

    List<Bid> findByStatus(BidStatus status);

    List<Bid> findByLoadIdAndStatus(UUID loadId, BidStatus status);

}

