package com.harsha.tms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "loads")
public class Load {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Version
    private long version;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String deliveryLocation;

    @Column(nullable = false)
    private BigDecimal weight;

    @Column(nullable = false)
    private String cargoType;

    @Column(nullable = false)
    private LocalDateTime pickupDate;

    @Column(nullable = false)
    private LocalDateTime deliveryDate;

    @Column(nullable = false)
    private BigDecimal offeredPrice;

    @Column(nullable = false)
    private Integer trucksRequired;

    @Column(nullable = false)
    private Integer remainingTrucks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime datePosted;

    @OneToMany(mappedBy = "load", fetch = FetchType.LAZY)
    private List<Bid> bids;

    @OneToMany(mappedBy = "load", fetch = FetchType.LAZY)
    private List<Booking> bookings;

    public Load() {
    }

    public Load(UUID id, long version, String pickupLocation, String deliveryLocation, BigDecimal weight, String cargoType, LocalDateTime pickupDate, LocalDateTime deliveryDate, BigDecimal offeredPrice, Integer trucksRequired, Integer remainingTrucks, BookingStatus status, LocalDateTime datePosted, List<Bid> bids, List<Booking> bookings) {
        this.id = id;
        this.version = version;
        this.pickupLocation = pickupLocation;
        this.deliveryLocation = deliveryLocation;
        this.weight = weight;
        this.cargoType = cargoType;
        this.pickupDate = pickupDate;
        this.deliveryDate = deliveryDate;
        this.offeredPrice = offeredPrice;
        this.trucksRequired = trucksRequired;
        this.remainingTrucks = remainingTrucks;
        this.status = status;
        this.datePosted = datePosted;
        this.bids = bids;
        this.bookings = bookings;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public LocalDateTime getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDateTime pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public BigDecimal getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(BigDecimal offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public Integer getTrucksRequired() {
        return trucksRequired;
    }

    public void setTrucksRequired(Integer trucksRequired) {
        this.trucksRequired = trucksRequired;
    }

    public Integer getRemainingTrucks() {
        return remainingTrucks;
    }

    public void setRemainingTrucks(Integer remainingTrucks) {
        this.remainingTrucks = remainingTrucks;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(LocalDateTime datePosted) {
        this.datePosted = datePosted;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}

