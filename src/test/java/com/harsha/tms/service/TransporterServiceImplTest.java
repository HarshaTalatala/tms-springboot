package com.harsha.tms.service;

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
import static org.mockito.ArgumentMatchers.anyCollection;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.harsha.tms.dto.request.TransporterRequestDTO;
import com.harsha.tms.dto.request.UpdateTrucksRequestDTO;
import com.harsha.tms.dto.response.TransporterResponseDTO;
import com.harsha.tms.entity.Transporter;
import com.harsha.tms.entity.Truck;
import com.harsha.tms.exception.ResourceNotFoundException;
import com.harsha.tms.repository.TransporterRepository;
import com.harsha.tms.repository.TruckRepository;
import com.harsha.tms.service.impl.TransporterServiceImpl;

@ExtendWith(MockitoExtension.class)
class TransporterServiceImplTest {

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private TruckRepository truckRepository;

    @InjectMocks
    private TransporterServiceImpl transporterService;

    private UUID testTransporterId;
    private TransporterRequestDTO transporterRequestDTO;
    private Transporter transporter;
    private Truck truck1;
    private Truck truck2;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testTransporterId = UUID.randomUUID();

        transporterRequestDTO = new TransporterRequestDTO(
                "FastShip Logistics",
                4.5
        );

        transporter = new Transporter();
        transporter.setTransporterId(testTransporterId);
        transporter.setCompanyName("FastShip Logistics");
        transporter.setRating(4.5);

        truck1 = new Truck();
        truck1.setTruckType("Flatbed");
        truck1.setCount(5);
        truck1.setTransporter(transporter);

        truck2 = new Truck();
        truck2.setTruckType("Box Truck");
        truck2.setCount(3);
        truck2.setTransporter(transporter);
    }

    @Test
    void testCreateTransporter_Success() {
        when(transporterRepository.save(any(Transporter.class))).thenReturn(transporter);

        TransporterResponseDTO response = transporterService.createTransporter(transporterRequestDTO);

        assertNotNull(response);
        assertEquals(testTransporterId, response.transporterId());
        assertEquals("FastShip Logistics", response.companyName());
        assertEquals(4.5, response.rating());
        verify(transporterRepository, times(1)).save(any(Transporter.class));
    }

    @Test
    void testGetTransporterById_Success() {
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));

        TransporterResponseDTO response = transporterService.getTransporterById(testTransporterId);

        assertNotNull(response);
        assertEquals(testTransporterId, response.transporterId());
        assertEquals("FastShip Logistics", response.companyName());
        assertEquals(4.5, response.rating());
        verify(transporterRepository, times(1)).findById(testTransporterId);
    }

    @Test
    void testGetTransporterById_NotFound() {
        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transporterService.getTransporterById(testTransporterId));
        assertNotNull(exception);
        verify(transporterRepository, times(1)).findById(testTransporterId);
    }

    @Test
    void testUpdateTrucks_Success() {
        List<UpdateTrucksRequestDTO.TruckDTO> truckDTOs = Arrays.asList(
                new UpdateTrucksRequestDTO.TruckDTO("Flatbed", 10),
                new UpdateTrucksRequestDTO.TruckDTO("Box Truck", 5)
        );
        UpdateTrucksRequestDTO updateRequest = new UpdateTrucksRequestDTO(truckDTOs);

        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(truckRepository.findByTransporterTransporterId(testTransporterId))
                .thenReturn(Arrays.asList(truck1, truck2));

        TransporterResponseDTO response = transporterService.updateTrucks(testTransporterId, updateRequest);

        assertNotNull(response);
        assertEquals(testTransporterId, response.transporterId());
        verify(transporterRepository, times(1)).findById(testTransporterId);
        verify(truckRepository, times(1)).deleteAll(anyCollection());
        verify(truckRepository, times(2)).save(any(Truck.class));
    }

    @Test
    void testUpdateTrucks_TransporterNotFound() {
        List<UpdateTrucksRequestDTO.TruckDTO> truckDTOs = Arrays.asList(
                new UpdateTrucksRequestDTO.TruckDTO("Flatbed", 10)
        );
        UpdateTrucksRequestDTO updateRequest = new UpdateTrucksRequestDTO(truckDTOs);

        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transporterService.updateTrucks(testTransporterId, updateRequest));
        assertNotNull(exception);
        verify(transporterRepository, times(1)).findById(testTransporterId);
    }

    @Test
    void testUpdateTrucks_EmptyTruckList() {
        UpdateTrucksRequestDTO updateRequest = new UpdateTrucksRequestDTO(Arrays.asList());

        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(truckRepository.findByTransporterTransporterId(testTransporterId))
                .thenReturn(Arrays.asList(truck1, truck2));

        TransporterResponseDTO response = transporterService.updateTrucks(testTransporterId, updateRequest);

        assertNotNull(response);
        verify(truckRepository, times(1)).deleteAll(anyCollection());
        verify(truckRepository, times(0)).save(any(Truck.class));
    }

    @Test
    void testUpdateTrucks_ReplacesExistingTrucks() {
        List<UpdateTrucksRequestDTO.TruckDTO> newTrucks = Arrays.asList(
                new UpdateTrucksRequestDTO.TruckDTO("Refrigerated", 8)
        );
        UpdateTrucksRequestDTO updateRequest = new UpdateTrucksRequestDTO(newTrucks);

        when(transporterRepository.findById(testTransporterId)).thenReturn(Optional.of(transporter));
        when(truckRepository.findByTransporterTransporterId(testTransporterId))
                .thenReturn(Arrays.asList(truck1, truck2));

        TransporterResponseDTO response = transporterService.updateTrucks(testTransporterId, updateRequest);

        assertNotNull(response);
        verify(truckRepository, times(1)).deleteAll(Arrays.asList(truck1, truck2));
        verify(truckRepository, times(1)).save(any(Truck.class));
    }
}
