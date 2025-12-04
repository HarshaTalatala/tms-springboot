package com.harsha.tms.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.harsha.tms.dto.request.LoadRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.dto.response.LoadResponseDTO;

public interface LoadService {

    LoadResponseDTO createLoad(LoadRequestDTO request);

    LoadResponseDTO getLoadById(UUID loadId);

    Page<LoadResponseDTO> listLoads(Pageable pageable /*, optional filter params when defined */);

    LoadResponseDTO cancelLoad(UUID loadId);

    List<BidResponseDTO> getBestBids(UUID loadId);
}