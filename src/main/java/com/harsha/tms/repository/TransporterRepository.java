package com.harsha.tms.repository;

import com.harsha.tms.entity.Transporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransporterRepository extends JpaRepository<Transporter, UUID> {

    List<Transporter> findByCompanyName(String companyName);

    List<Transporter> findByRatingGreaterThanEqual(Double rating);

}

