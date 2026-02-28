package com.kerneldc.metarbatch.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class EmailQuotaServiceTest {

	@Test
	void testUnlimitedQuota() {
		var service = new EmailQuotaService(-1);
		
		assertThat(service.checkQuota(), is(true));
	}

	@Test
	void testQuota1() {
		var service = new EmailQuotaService(3);
		
		assertThat(service.checkQuota(), is(true));
		assertThat(service.checkQuota(), is(true));
		assertThat(service.checkQuota(), is(true));
		assertThat(service.checkQuota(), is(false));
	}

}
