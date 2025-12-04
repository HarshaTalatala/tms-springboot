package com.harsha.tms.service;

import com.harsha.tms.entity.BookingStatus;
import com.harsha.tms.exception.InvalidStatusTransitionException;

public class LoadStatusValidator {

    private LoadStatusValidator() {
        // Utility class
    }

    /**
     * Validates if an action can be performed on a load given its current status.
     * 
     * @param currentStatus the current status of the load
     * @param action the action being attempted
     * @throws InvalidStatusTransitionException if the action is not allowed
     */
    public static void validateStatusTransition(BookingStatus currentStatus, String action) {
        switch (action.toUpperCase()) {
            case "BID" -> {
                if (currentStatus == BookingStatus.BOOKED) {
                    throw new InvalidStatusTransitionException(
                            "Cannot bid on load with status BOOKED");
                }
                if (currentStatus == BookingStatus.CANCELLED) {
                    throw new InvalidStatusTransitionException(
                            "Cannot bid on load with status CANCELLED");
                }
            }
            case "CANCEL" -> {
                if (currentStatus == BookingStatus.BOOKED) {
                    throw new InvalidStatusTransitionException(
                            "Cannot cancel load with status BOOKED");
                }
            }
            case "BOOK" -> {
                if (currentStatus == BookingStatus.CANCELLED) {
                    throw new InvalidStatusTransitionException(
                            "Cannot book load with status CANCELLED");
                }
            }
            default -> {
                // No validation for other actions
            }
        }
    }

    /**
     * Validates that a load can transition to BOOKED status.
     * BOOKED status should only be set when remainingTrucks == 0.
     * 
     * @param remainingTrucks the remaining trucks count
     * @throws InvalidStatusTransitionException if remainingTrucks is not 0
     */
    public static void validateBookedStatus(Integer remainingTrucks) {
        if (remainingTrucks == null || remainingTrucks != 0) {
            throw new InvalidStatusTransitionException(
                    "Load can only be marked as BOOKED when remainingTrucks is 0");
        }
    }
}
