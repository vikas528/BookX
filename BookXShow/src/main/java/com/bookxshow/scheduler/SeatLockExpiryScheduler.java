/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Scheduled task that sweeps and releases expired seat locks.
 *
 * @since 1.0.0
 */
package com.bookxshow.scheduler;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bookxshow.repository.SeatRepository;

/**
 * Background scheduler that periodically releases seats whose lock TTL
 * has expired.
 *
 * <p>Runs at a fixed interval defined by
 * {@code bookxshow.booking.lock-sweep-interval-ms}. Each invocation
 * executes a single bulk {@code UPDATE} statement, making it efficient
 * even with large seat inventories.</p>
 *
 * <p>This scheduler is registered as a Spring bean with
 * {@link org.springframework.scheduling.annotation.EnableScheduling}
 * enabled on the application class.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class SeatLockExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(SeatLockExpiryScheduler.class);

    private final SeatRepository seatRepository;

    /**
     * Constructs the scheduler with the seat repository.
     *
     * @param seatRepository seat data-access layer
     */
    public SeatLockExpiryScheduler(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    /**
     * Safety-net sweep that bulk-releases any seats whose lock TTL has expired
     * but were not self-healed by the booking hot-path.
     *
     * <p>This is intentionally a <em>long-interval</em> background job — it is
     * <strong>not</strong> the primary mechanism for releasing locks. The
     * {@code SeatLockManager.lockSeat()} method performs an inline expiry check
     * so that expired locks are reclaimed lazily without any scheduler
     * involvement on the critical booking path.</p>
     *
     * <p>This sweep handles only true edge cases: seats that expired but were
     * never re-contested (e.g. a show where demand dropped off, or a JVM crash
     * before the lazy check could fire). Running it every 60 s is sufficient
     * and avoids the lock-contention spikes a 5 s interval would cause during
     * peak load with thousands of concurrent {@code SELECT … FOR UPDATE}
     * transactions.</p>
     *
     * <p>Interval is controlled by
     * {@code bookxshow.booking.lock-sweep-interval-ms} (default 60 000 ms).</p>
     */
    @Scheduled(fixedDelayString = "${bookxshow.booking.lock-sweep-interval-ms:60000}")
    @Transactional
    public void releaseExpiredLocks() {
        int released = seatRepository.releaseExpiredLocks(Instant.now());
        if (released > 0) {
            log.info("Safety-net sweep released {} stale expired seat lock(s)", released);
        } else {
            log.debug("Safety-net sweep: no stale locks found");
        }
    }
}
