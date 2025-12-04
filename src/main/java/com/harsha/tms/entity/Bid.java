package com.harsha.tms.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "bids", 
    indexes = {
        @Index(name = "idx_bid_load_id", columnList = "load_id"),
        @Index(name = "idx_bid_transporter_id", columnList = "transporter_id"),
        @Index(name = "idx_bid_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_accepted_bid",
            columnNames = {"load_id", "status"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"load", "transporter"})
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID bidId;

    private Double proposedRate;

    private Integer trucksOffered;

    private String truckType;

    @Enumerated(EnumType.STRING)
    private BidStatus status;

    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "load_id", nullable = false)
    private Load load;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transporter_id", nullable = false)
    private Transporter transporter;
}

