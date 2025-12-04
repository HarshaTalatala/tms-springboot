package com.harsha.tms.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harsha.tms.dto.request.BidRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.service.BidService;

@RestController
@RequestMapping("/bid")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<BidResponseDTO> submitBid(@RequestBody BidRequestDTO request) {
        BidResponseDTO response = bidService.submitBid(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BidResponseDTO>> listBids() {
        List<BidResponseDTO> response = bidService.listBids();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BidResponseDTO> getBidById(@PathVariable UUID id) {
        BidResponseDTO response = bidService.getBidById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<BidResponseDTO> rejectBid(@PathVariable UUID id) {
        BidResponseDTO response = bidService.rejectBid(id);
        return ResponseEntity.ok(response);
    }
}
