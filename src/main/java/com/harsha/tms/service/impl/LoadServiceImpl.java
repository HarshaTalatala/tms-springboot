package com.harsha.tms.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harsha.tms.dto.request.LoadRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.dto.response.LoadResponseDTO;
import com.harsha.tms.entity.Bid;
import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.Load;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.BidRepository;
import com.harsha.tms.repository.LoadRepository;
import com.harsha.tms.service.LoadStatusValidator;
import com.harsha.tms.service.ScoreWeights;

@Service
public class LoadServiceImpl implements com.harsha.tms.service.LoadService {

    private final LoadRepository loadRepository;
    private final BidRepository bidRepository;
    private final Clock clock;

    public LoadServiceImpl(LoadRepository loadRepository, BidRepository bidRepository, Clock clock) {
        this.loadRepository = loadRepository;
        this.bidRepository = bidRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public LoadResponseDTO createLoad(LoadRequestDTO request) {
        Load load = new Load();
        load.setShipperId(request.shipperId());
        load.setPickupLocation(request.pickupLocation());
        load.setDeliveryLocation(request.deliveryLocation());
        load.setWeight(request.weight());
        load.setWeightUnit(request.weightUnit());
        load.setCargoType(request.cargoType());
        load.setPickupDate(request.pickupDate());
        load.setDeliveryDate(request.deliveryDate());
        load.setOfferedPrice(request.offeredPrice());
        load.setTrucksRequired(request.trucksRequired());
        load.setRemainingTrucks(request.trucksRequired());
        load.setStatus(BookingStatus.POSTED);
        load.setDatePosted(LocalDateTime.now(clock));
        
        Load savedLoad = loadRepository.save(load);
        
        return new LoadResponseDTO(
                savedLoad.getId(),
                savedLoad.getShipperId(),
                savedLoad.getPickupLocation(),
                savedLoad.getDeliveryLocation(),
                savedLoad.getWeight(),
                savedLoad.getWeightUnit(),
                savedLoad.getCargoType(),
                savedLoad.getPickupDate(),
                savedLoad.getDeliveryDate(),
                savedLoad.getOfferedPrice(),
                savedLoad.getTrucksRequired(),
                savedLoad.getRemainingTrucks(),
                savedLoad.getStatus(),
                savedLoad.getDatePosted()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoadResponseDTO getLoadById(java.util.UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));
        
        return new LoadResponseDTO(
                load.getId(),
                load.getShipperId(),
                load.getPickupLocation(),
                load.getDeliveryLocation(),
                load.getWeight(),
                load.getWeightUnit(),
                load.getCargoType(),
                load.getPickupDate(),
                load.getDeliveryDate(),
                load.getOfferedPrice(),
                load.getTrucksRequired(),
                load.getRemainingTrucks(),
                load.getStatus(),
                load.getDatePosted()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoadResponseDTO> listLoads(java.util.UUID shipperId, BookingStatus status, Pageable pageable) {
        List<Load> loads;
        
        if (shipperId != null && status != null) {
            loads = loadRepository.findByShipperIdAndStatus(shipperId, status);
        } else if (shipperId != null) {
            loads = loadRepository.findByShipperId(shipperId);
        } else if (status != null) {
            loads = loadRepository.findByStatus(status);
        } else {
            return loadRepository.findAll(pageable).map(this::toLoadResponseDTO);
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), loads.size());
        List<LoadResponseDTO> pageContent = loads.subList(start, end).stream()
                .map(this::toLoadResponseDTO)
                .toList();
        
        return new org.springframework.data.domain.PageImpl<>(
                pageContent,
                pageable,
                loads.size()
        );
    }

    @Override
    @Transactional
    public LoadResponseDTO cancelLoad(java.util.UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));
        
        LoadStatusValidator.validateStatusTransition(load.getStatus(), "CANCEL");
        
        load.setStatus(BookingStatus.CANCELLED);
        Load savedLoad = loadRepository.save(load);
        
        return new LoadResponseDTO(
                savedLoad.getId(),
                savedLoad.getShipperId(),
                savedLoad.getPickupLocation(),
                savedLoad.getDeliveryLocation(),
                savedLoad.getWeight(),
                savedLoad.getWeightUnit(),
                savedLoad.getCargoType(),
                savedLoad.getPickupDate(),
                savedLoad.getDeliveryDate(),
                savedLoad.getOfferedPrice(),
                savedLoad.getTrucksRequired(),
                savedLoad.getRemainingTrucks(),
                savedLoad.getStatus(),
                savedLoad.getDatePosted()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidResponseDTO> getBestBids(java.util.UUID loadId) {
        loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));
        
        List<Bid> bids = bidRepository.findByLoad_Id(loadId);
        
        return bids.stream()
                .map(bid -> {
                    Double ratingObj = bid.getTransporter().getRating();
                    double rating = ratingObj != null ? ratingObj : 0.0;
                    Double proposedRateObj = bid.getProposedRate();
                    double proposedRate = proposedRateObj != null ? proposedRateObj : Double.MAX_VALUE;
                    double score = ScoreWeights.PRICE_WEIGHT * (1.0 / proposedRate) + 
                                   ScoreWeights.RATING_WEIGHT * (rating / ScoreWeights.MAX_RATING);
                    return new BidWithScore(bid, score);
                })
                .sorted(Comparator.comparingDouble(BidWithScore::score).reversed())
                .map(bidWithScore -> new BidResponseDTO(
                        bidWithScore.bid().getBidId(),
                        bidWithScore.bid().getLoad().getId(),
                        bidWithScore.bid().getTransporter().getTransporterId(),
                        bidWithScore.bid().getProposedRate(),
                        bidWithScore.bid().getTrucksOffered(),
                        bidWithScore.bid().getTruckType(),
                        bidWithScore.bid().getStatus(),
                        bidWithScore.bid().getSubmittedAt()
                ))
                .toList();
    }
    
    private LoadResponseDTO toLoadResponseDTO(Load load) {
        return new LoadResponseDTO(
                load.getId(),
                load.getShipperId(),
                load.getPickupLocation(),
                load.getDeliveryLocation(),
                load.getWeight(),
                load.getWeightUnit(),
                load.getCargoType(),
                load.getPickupDate(),
                load.getDeliveryDate(),
                load.getOfferedPrice(),
                load.getTrucksRequired(),
                load.getRemainingTrucks(),
                load.getStatus(),
                load.getDatePosted()
        );
    }
    
    private record BidWithScore(Bid bid, double score) {}
}
