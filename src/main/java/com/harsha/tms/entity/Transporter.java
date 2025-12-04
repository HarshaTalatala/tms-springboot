package com.harsha.tms.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transporters")
public class Transporter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID transporterId;

    private String companyName;

    private Double rating;

    @OneToMany(mappedBy = "transporter", fetch = FetchType.LAZY)
    private List<Truck> availableTrucks;

    @OneToMany(mappedBy = "transporter", fetch = FetchType.LAZY)
    private List<Bid> bids;

    @OneToMany(mappedBy = "transporter", fetch = FetchType.LAZY)
    private List<Booking> bookings;

    public Transporter() {
    }

    public Transporter(UUID transporterId, String companyName, Double rating, List<Truck> availableTrucks, List<Bid> bids, List<Booking> bookings) {
        this.transporterId = transporterId;
        this.companyName = companyName;
        this.rating = rating;
        this.availableTrucks = availableTrucks;
        this.bids = bids;
        this.bookings = bookings;
    }

    public UUID getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(UUID transporterId) {
        this.transporterId = transporterId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<Truck> getAvailableTrucks() {
        return availableTrucks;
    }

    public void setAvailableTrucks(List<Truck> availableTrucks) {
        this.availableTrucks = availableTrucks;
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

