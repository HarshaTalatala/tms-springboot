package com.harsha.tms.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.harsha.tms.dto.request.BidRequestDTO;
import com.harsha.tms.dto.request.BookingRequestDTO;
import com.harsha.tms.dto.request.LoadRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.dto.response.BookingResponseDTO;
import com.harsha.tms.dto.response.LoadResponseDTO;

public interface LoadService {

    LoadResponseDTO createLoad(LoadRequestDTO request);

    LoadResponseDTO getLoadById(UUID loadId);

    Page<LoadResponseDTO> listLoads(Pageable pageable /*, optional filter params when defined */);

    LoadResponseDTO cancelLoad(UUID loadId);

    List<BidResponseDTO> getBestBids(UUID loadId);

    BidResponseDTO submitBid(BidRequestDTO request);

    BidResponseDTO rejectBid(UUID bidId);

    BookingResponseDTO createBooking(BookingRequestDTO request);

    BookingResponseDTO cancelBooking(UUID bookingId);
}