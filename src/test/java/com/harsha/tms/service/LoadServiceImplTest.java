package com.harsha.tms.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.harsha.tms.dto.request.LoadRequestDTO;
import com.harsha.tms.dto.response.LoadResponseDTO;
import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.entity.Load;
import com.harsha.tms.entity.WeightUnit;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.LoadRepository;
import com.harsha.tms.service.impl.LoadServiceImpl;

@ExtendWith(MockitoExtension.class)
class LoadServiceImplTest {

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private LoadServiceImpl loadService;

    private UUID testShipperId;
    private UUID testLoadId;
    private LoadRequestDTO loadRequestDTO;
    private Load load;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testShipperId = UUID.randomUUID();
        testLoadId = UUID.randomUUID();
        
        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        loadRequestDTO = new LoadRequestDTO(
                testShipperId,
                "New York",
                "Los Angeles",
                BigDecimal.valueOf(1000),
                WeightUnit.KG,
                "Electronics",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                BigDecimal.valueOf(5000),
                2
        );

        load = new Load();
        load.setId(testLoadId);
        load.setShipperId(testShipperId);
        load.setPickupLocation("New York");
        load.setDeliveryLocation("Los Angeles");
        load.setWeight(BigDecimal.valueOf(1000));
        load.setWeightUnit(WeightUnit.KG);
        load.setCargoType("Electronics");
        load.setPickupDate(LocalDateTime.now().plusDays(1));
        load.setDeliveryDate(LocalDateTime.now().plusDays(5));
        load.setOfferedPrice(BigDecimal.valueOf(5000));
        load.setTrucksRequired(2);
        load.setRemainingTrucks(2);
        load.setStatus(BookingStatus.POSTED);
        load.setDatePosted(LocalDateTime.now(clock));
    }

    @Test
    void testCreateLoad_Success() {
        when(loadRepository.save(any(Load.class))).thenReturn(load);

        LoadResponseDTO response = loadService.createLoad(loadRequestDTO);

        assertNotNull(response);
        assertEquals(testShipperId, response.shipperId());
        assertEquals("New York", response.pickupLocation());
        assertEquals("Los Angeles", response.deliveryLocation());
        assertEquals(BigDecimal.valueOf(1000), response.weight());
        assertEquals(WeightUnit.KG, response.weightUnit());
        assertEquals("Electronics", response.cargoType());
        assertEquals(BookingStatus.POSTED, response.status());
        verify(loadRepository, times(1)).save(any(Load.class));
    }

    @Test
    void testCreateLoad_WithShipperId() {
        when(loadRepository.save(any(Load.class))).thenReturn(load);

        LoadResponseDTO response = loadService.createLoad(loadRequestDTO);

        assertNotNull(response);
        assertEquals(testShipperId, response.shipperId());
        verify(loadRepository, times(1)).save(any(Load.class));
    }

    @Test
    void testCreateLoad_WithWeightUnit() {
        when(loadRepository.save(any(Load.class))).thenReturn(load);

        LoadResponseDTO response = loadService.createLoad(loadRequestDTO);

        assertNotNull(response);
        assertEquals(WeightUnit.KG, response.weightUnit());
        verify(loadRepository, times(1)).save(any(Load.class));
    }

    @Test
    void testGetLoadById_Success() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));

        LoadResponseDTO response = loadService.getLoadById(testLoadId);

        assertNotNull(response);
        assertEquals(testLoadId, response.id());
        assertEquals(testShipperId, response.shipperId());
        verify(loadRepository, times(1)).findById(testLoadId);
    }

    @Test
    void testGetLoadById_NotFound() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> loadService.getLoadById(testLoadId));
        assertNotNull(exception);
        verify(loadRepository, times(1)).findById(testLoadId);
    }

    @Test
    void testListLoads_NoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Load> page = new PageImpl<>(Arrays.asList(load));
        when(loadRepository.findAll(pageable)).thenReturn(page);

        Page<LoadResponseDTO> response = loadService.listLoads(null, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(loadRepository, times(1)).findAll(pageable);
    }

    @Test
    void testListLoads_FilterByShipperId() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loadRepository.findByShipperId(testShipperId)).thenReturn(Arrays.asList(load));

        Page<LoadResponseDTO> response = loadService.listLoads(testShipperId, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(testShipperId, response.getContent().get(0).shipperId());
        verify(loadRepository, times(1)).findByShipperId(testShipperId);
    }

    @Test
    void testListLoads_FilterByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loadRepository.findByStatus(BookingStatus.POSTED)).thenReturn(Arrays.asList(load));

        Page<LoadResponseDTO> response = loadService.listLoads(null, BookingStatus.POSTED, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(BookingStatus.POSTED, response.getContent().get(0).status());
        verify(loadRepository, times(1)).findByStatus(BookingStatus.POSTED);
    }

    @Test
    void testListLoads_FilterByShipperIdAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loadRepository.findByShipperIdAndStatus(testShipperId, BookingStatus.POSTED))
                .thenReturn(Arrays.asList(load));

        Page<LoadResponseDTO> response = loadService.listLoads(testShipperId, BookingStatus.POSTED, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(loadRepository, times(1)).findByShipperIdAndStatus(testShipperId, BookingStatus.POSTED);
    }

    @Test
    void testCancelLoad_Success() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(load));
        load.setStatus(BookingStatus.CANCELLED);
        when(loadRepository.save(any(Load.class))).thenReturn(load);

        LoadResponseDTO response = loadService.cancelLoad(testLoadId);

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.status());
        verify(loadRepository, times(1)).findById(testLoadId);
        verify(loadRepository, times(1)).save(any(Load.class));
    }

    @Test
    void testCancelLoad_NotFound() {
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> loadService.cancelLoad(testLoadId));
        assertNotNull(exception);
        verify(loadRepository, times(1)).findById(testLoadId);
    }
}
