package com.harsha.tms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID bookingId;

    @Column(name = "load_id")
    private UUID loadId;

    @Column(name = "bid_id")
    private UUID bidId;

    @Column(name = "transporter_id")
    private UUID transporterId;

    private Integer allocatedTrucks;

    private Double finalRate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime bookedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "load_id", insertable = false, updatable = false)
    private Load load;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transporter_id", insertable = false, updatable = false)
    private Transporter transporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", insertable = false, updatable = false)
    private Bid bid;


    // ------------ Constructors ------------ //

    public Booking() {
    }

    public Booking(UUID bookingId,
                   UUID loadId,
                   UUID bidId,
                   UUID transporterId,
                   Integer allocatedTrucks,
                   Double finalRate,
                   BookingStatus status,
                   LocalDateTime bookedAt,
                   Load load,
                   Transporter transporter,
                   Bid bid) {

        this.bookingId = bookingId;
        this.loadId = loadId;
        this.bidId = bidId;
        this.transporterId = transporterId;
        this.allocatedTrucks = allocatedTrucks;
        this.finalRate = finalRate;
        this.status = status;
        this.bookedAt = bookedAt;
        this.load = load;
        this.transporter = transporter;
        this.bid = bid;
    }


    // ------------ Getters & Setters ------------ //

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getLoadId() {
        return loadId;
    }

    public void setLoadId(UUID loadId) {
        this.loadId = loadId;
    }

    public UUID getBidId() {
        return bidId;
    }

    public void setBidId(UUID bidId) {
        this.bidId = bidId;
    }

    public UUID getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(UUID transporterId) {
        this.transporterId = transporterId;
    }

    public Integer getAllocatedTrucks() {
        return allocatedTrucks;
    }

    public void setAllocatedTrucks(Integer allocatedTrucks) {
        this.allocatedTrucks = allocatedTrucks;
    }

    public Double getFinalRate() {
        return finalRate;
    }

    public void setFinalRate(Double finalRate) {
        this.finalRate = finalRate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
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

    public Bid getBid() {
        return bid;
    }

    public void setBid(Bid bid) {
        this.bid = bid;
    }
}

