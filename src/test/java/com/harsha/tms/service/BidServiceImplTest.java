package com.harsha.tms.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.harsha.tms.dto.request.BidRequestDTO;
import com.harsha.tms.dto.response.BidResponseDTO;
import com.harsha.tms.entity.Bid;
import com.harsha.tms.entity.BidStatus;
import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.Load;
import com.harsha.tms.entity.Transporter;
import com.harsha.tms.entity.Truck;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.BidRepository;
import com.harsha.tms.repository.LoadRepository;
import com.harsha.tms.repository.TransporterRepository;
import com.harsha.tms.repository.TruckRepository;
import com.harsha.tms.service.impl.BidServiceImpl;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private BidServiceImpl bidService;

    private UUID testBidId;
    private UUID testLoadId;
    private UUID testTransporterId;
    private BidRequestDTO bidRequestDTO;
    private Bid bid;
    private Load load;
    private Transporter transporter;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testBidId = UUID.randomUUID();
        testLoadId = UUID.randomUUID();
        testTransporterId = UUID.randomUUID();

        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        bidRequestDTO = new BidRequestDTO(
                testLoadId,
                testTransporterId,
                4500.0,
                2,
                "Flatbed"
        );

        load = new Load();
        load.setId(testLoadId);
        load.setStatus(BookingStatus.POSTED);

        transporter = new Transporter();
        transporter.setTransporterId(testTransporterId);
        transporter.setRating(4.5);

        bid = new Bid();
        bid.setBidId(testBidId);
        bid.setLoad(load);
        bid.setTransporter(transporter);
        bid.setProposedRate(4500.0);
        bid.setTrucksOffered(2);
        bid.setTruckType("Flatbed");
        bid.setStatus(BidStatus.PENDING);
        bid.setSubmittedAt(LocalDateTime.now(clock));

        Truck truck = new Truck();
        truck.setTruckType("Flatbed");
        truck.setCount(5);
        lenient().when(truckRepository.findByTransporterTransporterId(testTransporterId))
                .thenReturn(Arrays.asList(truck));
    }

    @Test
    void testSubmitBid_Success() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(bidRepository.findByLoad_Id(testLoadId)).thenReturn(Arrays.asList());
        when(bidRepository.findByLoad_IdAndStatus(testLoadId, BidStatus.ACCEPTED)).thenReturn(Arrays.asList());
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        BidResponseDTO response = bidService.submitBid(bidRequestDTO);

        assertNotNull(response);
        assertEquals(testLoadId, response.loadId());
        assertEquals(testTransporterId, response.transporterId());
        assertEquals(4500.0, response.proposedRate());
        assertEquals(BidStatus.PENDING, response.status());
        verify(bidRepository, times(1)).save(any(Bid.class));
    }

    @Test
    void testSubmitBid_LoadNotFound() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> bidService.submitBid(bidRequestDTO));
        assertNotNull(exception);
        verify(loadRepository, times(1)).findById(testLoadId);
    }

    @Test
    void testSubmitBid_TransporterNotFound() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> bidService.submitBid(bidRequestDTO));
        assertNotNull(exception);
        verify(transporterRepository, times(1)).findById(testTransporterId);
    }

    @Test
    void testSubmitBid_DuplicateAcceptedBid() {
        Bid acceptedBid = new Bid();
        acceptedBid.setStatus(BidStatus.ACCEPTED);
        
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(bidRepository.findByLoad_IdAndStatus(testLoadId, BidStatus.ACCEPTED))
                .thenReturn(Arrays.asList(acceptedBid));

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> bidService.submitBid(bidRequestDTO));
        assertNotNull(exception);
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void testListBids_NoFilters() {
        when(bidRepository.findAll()).thenReturn(Arrays.asList(bid));

        List<BidResponseDTO> response = bidService.listBids(null, null, null);

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(bidRepository, times(1)).findAll();
    }

    @Test
    void testListBids_FilterByLoadId() {
        when(bidRepository.findByLoad_Id(testLoadId)).thenReturn(Arrays.asList(bid));

        List<BidResponseDTO> response = bidService.listBids(testLoadId, null, null);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testLoadId, response.get(0).loadId());
        verify(bidRepository, times(1)).findByLoad_Id(testLoadId);
    }

    @Test
    void testListBids_FilterByTransporterId() {
        when(bidRepository.findByTransporter_TransporterId(testTransporterId)).thenReturn(Arrays.asList(bid));

        List<BidResponseDTO> response = bidService.listBids(null, testTransporterId, null);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testTransporterId, response.get(0).transporterId());
        verify(bidRepository, times(1)).findByTransporter_TransporterId(testTransporterId);
    }

    @Test
    void testListBids_FilterByStatus() {
        when(bidRepository.findByStatus(BidStatus.PENDING)).thenReturn(Arrays.asList(bid));

        List<BidResponseDTO> response = bidService.listBids(null, null, BidStatus.PENDING);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(BidStatus.PENDING, response.get(0).status());
        verify(bidRepository, times(1)).findByStatus(BidStatus.PENDING);
    }

    @Test
    void testListBids_FilterByLoadIdAndStatus() {
        when(bidRepository.findByLoad_IdAndStatus(testLoadId, BidStatus.PENDING))
                .thenReturn(Arrays.asList(bid));

        List<BidResponseDTO> response = bidService.listBids(testLoadId, null, BidStatus.PENDING);

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(bidRepository, times(1)).findByLoad_IdAndStatus(testLoadId, BidStatus.PENDING);
    }

    @Test
    void testListBids_FilterByTransporterIdAndStatus() {
        when(bidRepository.findByTransporter_TransporterIdAndStatus(testTransporterId, BidStatus.PENDING))
                .thenReturn(Arrays.asList(bid));

        List<BidResponseDTO> response = bidService.listBids(null, testTransporterId, BidStatus.PENDING);

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(bidRepository, times(1)).findByTransporter_TransporterIdAndStatus(testTransporterId, BidStatus.PENDING);
    }

    @Test
    void testListBids_FilterByAllParameters() {
        when(bidRepository.findByLoad_IdAndTransporter_TransporterIdAndStatus(testLoadId, testTransporterId, BidStatus.PENDING))
                .thenReturn(Arrays.asList(bid));

        List<BidResponseDTO> response = bidService.listBids(testLoadId, testTransporterId, BidStatus.PENDING);

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(bidRepository, times(1))
                .findByLoad_IdAndTransporter_TransporterIdAndStatus(testLoadId, testTransporterId, BidStatus.PENDING);
    }

    @Test
    void testGetBidById_Success() {
        when(bidRepository.findById(testBidId)).thenReturn(Optional.of(bid));

        BidResponseDTO response = bidService.getBidById(testBidId);

        assertNotNull(response);
        assertEquals(testBidId, response.bidId());
        verify(bidRepository, times(1)).findById(testBidId);
    }

    @Test
    void testGetBidById_NotFound() {
        when(bidRepository.findById(testBidId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> bidService.getBidById(testBidId));
        assertNotNull(exception);
        verify(bidRepository, times(1)).findById(testBidId);
    }

    @Test
    void testRejectBid_Success() {
        when(bidRepository.findById(testBidId)).thenReturn(Optional.of(bid));
        bid.setStatus(BidStatus.REJECTED);
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        BidResponseDTO response = bidService.rejectBid(testBidId);

        assertNotNull(response);
        assertEquals(BidStatus.REJECTED, response.status());
        verify(bidRepository, times(1)).findById(testBidId);
        verify(bidRepository, times(1)).save(any(Bid.class));
    }
}
