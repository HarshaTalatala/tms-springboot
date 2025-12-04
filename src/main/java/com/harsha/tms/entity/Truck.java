package com.harsha.tms.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "trucks")
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String truckType;

    private Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transporter_id")
    private Transporter transporter;

    public Truck() {
    }

    public Truck(UUID id, String truckType, Integer count, Transporter transporter) {
        this.id = id;
        this.truckType = truckType;
        this.count = count;
        this.transporter = transporter;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTruckType() {
        return truckType;
    }

    public void setTruckType(String truckType) {
        this.truckType = truckType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Transporter getTransporter() {
        return transporter;
    }

    public void setTransporter(Transporter transporter) {
        this.transporter = transporter;
    }
}

