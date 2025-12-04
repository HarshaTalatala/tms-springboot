package com.harsha.tms.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harsha.tms.dto.request.LoadRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.dto.response.LoadResponseDTO;
import com.harsha.tms.service.LoadService;

@RestController
@RequestMapping("/load")
public class LoadController {

    private final LoadService loadService;

    public LoadController(LoadService loadService) {
        this.loadService = loadService;
    }

    @PostMapping
    public ResponseEntity<LoadResponseDTO> createLoad(@RequestBody LoadRequestDTO request) {
        LoadResponseDTO response = loadService.createLoad(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<LoadResponseDTO>> listLoads(Pageable pageable) {
        Page<LoadResponseDTO> response = loadService.listLoads(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoadResponseDTO> getLoadById(@PathVariable UUID id) {
        LoadResponseDTO response = loadService.getLoadById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<LoadResponseDTO> cancelLoad(@PathVariable UUID id) {
        LoadResponseDTO response = loadService.cancelLoad(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/best-bids")
    public ResponseEntity<List<BidResponseDTO>> getBestBids(@PathVariable UUID id) {
        List<BidResponseDTO> response = loadService.getBestBids(id);
        return ResponseEntity.ok(response);
    }
}
