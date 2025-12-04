package com.harsha.tms.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transporters")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}

