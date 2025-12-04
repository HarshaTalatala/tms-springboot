package com.harsha.tms.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harsha.tms.dto.request.TransporterRequestDTO;
import com.harsha.tms.dto.request.UpdateTrucksRequestDTO;
import com.harsha.tms.dto.response.TransporterResponseDTO;
import com.harsha.tms.service.TransporterService;

@RestController
@RequestMapping("/transporter")
public class TransporterController {

    private final TransporterService transporterService;

    public TransporterController(TransporterService transporterService) {
        this.transporterService = transporterService;
    }

    @PostMapping
    public ResponseEntity<TransporterResponseDTO> createTransporter(@RequestBody TransporterRequestDTO request) {
        TransporterResponseDTO response = transporterService.createTransporter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransporterResponseDTO> getTransporterById(@PathVariable UUID id) {
        TransporterResponseDTO response = transporterService.getTransporterById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/trucks")
    public ResponseEntity<TransporterResponseDTO> updateTrucks(
            @PathVariable UUID id,
            @RequestBody UpdateTrucksRequestDTO request) {
        TransporterResponseDTO response = transporterService.updateTrucks(id, request);
        return ResponseEntity.ok(response);
    }
}
