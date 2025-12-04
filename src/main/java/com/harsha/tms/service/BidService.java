package com.harsha.tms.service;

import java.util.List;
import java.util.UUID;

import com.harsha.tms.dto.request.BidRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.entity.BidStatus;

public interface BidService {

    BidResponseDTO submitBid(BidRequestDTO request);

    List<BidResponseDTO> listBids(UUID loadId, UUID transporterId, BidStatus status);

    BidResponseDTO getBidById(UUID bidId);

    BidResponseDTO rejectBid(UUID bidId);
}
