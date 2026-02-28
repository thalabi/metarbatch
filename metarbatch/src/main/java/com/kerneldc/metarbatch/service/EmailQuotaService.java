package com.kerneldc.metarbatch.service;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
//@RequiredArgsConstructor
@Slf4j
public class EmailQuotaService {

	@Value("${application.email.dailyquota:-1}") // -1 means no quota
	private final Integer dailyQuota;

	private record QuotaStats(LocalDate date, AtomicInteger emailsSent) {}

	private volatile QuotaStats quotaStats = new QuotaStats(LocalDate.now(), new AtomicInteger());

	public EmailQuotaService(@Value("${application.email.dailyquota:-1}") Integer dailyQuota) {// -1 means no quota) 
		this.dailyQuota = dailyQuota;
	}
	
	public boolean checkQuota() {

        QuotaStats stats = refreshIfNewDay();

        // unlimited quota
        if (dailyQuota < 0) {
            return true;
        }

        AtomicInteger counter = stats.emailsSent();

        while (true) {

            int current = counter.get();

            if (current >= dailyQuota) {
                return false;
            }

            if (counter.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }
	
	private QuotaStats refreshIfNewDay() {

	    LocalDate today = LocalDate.now();
	    QuotaStats stats = quotaStats; // copy into a local variable to obtain a stable snapshot

	    if (!stats.date().equals(today)) { // check first outside synchronized block to improve performance. Most of the time returns false
	        synchronized (this) {
	            if (!quotaStats.date().equals(today)) {
	                quotaStats =
	                    new QuotaStats(today, new AtomicInteger());
	            }
	            stats = quotaStats;
	        }
	    }

	    return stats;
	}
}
