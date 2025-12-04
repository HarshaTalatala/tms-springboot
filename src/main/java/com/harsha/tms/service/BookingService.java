package com.harsha.tms.service;

import java.util.UUID;

import com.harsha.tms.dto.request.BookingRequestDTO;
import com.harsha.tms.dto.response.BookingResponseDTO;

public interface BookingService {

    BookingResponseDTO createBooking(BookingRequestDTO request);

    BookingResponseDTO getBookingById(UUID bookingId);

    BookingResponseDTO cancelBooking(UUID bookingId);
}
