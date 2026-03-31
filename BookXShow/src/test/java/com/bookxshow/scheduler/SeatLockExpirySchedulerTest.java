/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for SeatLockExpiryScheduler.
 *
 * @since 1.0.0
 */
package com.bookxshow.scheduler;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookxshow.repository.SeatRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SeatLockExpiryScheduler}.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SeatLockExpirySchedulerTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatLockExpiryScheduler scheduler;

    @Test
    @DisplayName("T01 - releaseExpiredLocks calls repository with current time")
    void releaseExpiredLocks_callsRepository() {
        when(seatRepository.releaseExpiredLocks(any(Instant.class))).thenReturn(0);

        Instant before = Instant.now();
        scheduler.releaseExpiredLocks();
        Instant after = Instant.now();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(seatRepository).releaseExpiredLocks(captor.capture());

        Instant captured = captor.getValue();
        assertFalse(captured.isBefore(before));
        assertFalse(captured.isAfter(after));
    }

    @Test
    @DisplayName("T02 - releaseExpiredLocks handles released seats > 0")
    void releaseExpiredLocks_withReleasedSeats() {
        when(seatRepository.releaseExpiredLocks(any(Instant.class))).thenReturn(3);

        // Should not throw — logging is the only side effect
        assertDoesNotThrow(() -> scheduler.releaseExpiredLocks());

        verify(seatRepository).releaseExpiredLocks(any(Instant.class));
    }

    @Test
    @DisplayName("T03 - releaseExpiredLocks handles zero released seats")
    void releaseExpiredLocks_withNoReleasedSeats() {
        when(seatRepository.releaseExpiredLocks(any(Instant.class))).thenReturn(0);

        assertDoesNotThrow(() -> scheduler.releaseExpiredLocks());

        verify(seatRepository).releaseExpiredLocks(any(Instant.class));
    }
}
