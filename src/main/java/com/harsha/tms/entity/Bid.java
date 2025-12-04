package com.harsha.tms.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID bidId;

    @Column(name = "load_id")
    private UUID loadId;

    @Column(name = "transporter_id")
    private UUID transporterId;

    private Double proposedRate;

    private Integer trucksOffered;

    private String truckType;

    @Enumerated(EnumType.STRING)
    private BidStatus status;

    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "load_id", insertable = false, updatable = false)
    private Load load;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transporter_id", insertable = false, updatable = false)
    private Transporter transporter;

    public Bid() {
    }

    public Bid(UUID bidId, UUID loadId, UUID transporterId, Double proposedRate, Integer trucksOffered, String truckType, BidStatus status, LocalDateTime submittedAt, Load load, Transporter transporter) {
        this.bidId = bidId;
        this.loadId = loadId;
        this.transporterId = transporterId;
        this.proposedRate = proposedRate;
        this.trucksOffered = trucksOffered;
        this.truckType = truckType;
        this.status = status;
        this.submittedAt = submittedAt;
        this.load = load;
        this.transporter = transporter;
    }

    public UUID getBidId() {
        return bidId;
    }

    public void setBidId(UUID bidId) {
        this.bidId = bidId;
    }

    public UUID getLoadId() {
        return loadId;
    }

    public void setLoadId(UUID loadId) {
        this.loadId = loadId;
    }

    public UUID getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(UUID transporterId) {
        this.transporterId = transporterId;
    }

    public Double getProposedRate() {
        return proposedRate;
    }

    public void setProposedRate(Double proposedRate) {
        this.proposedRate = proposedRate;
    }

    public Integer getTrucksOffered() {
        return trucksOffered;
    }

    public void setTrucksOffered(Integer trucksOffered) {
        this.trucksOffered = trucksOffered;
    }

    public String getTruckType() {
        return truckType;
    }

    public void setTruckType(String truckType) {
        this.truckType = truckType;
    }

    public BidStatus getStatus() {
        return status;
    }

    public void setStatus(BidStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load load) {
        this.load = load;
    }

    public Transporter getTransporter() {
        return transporter;
    }

    public void setTransporter(Transporter transporter) {
        this.transporter = transporter;
    }
}

