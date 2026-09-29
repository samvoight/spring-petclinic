/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for {@link InvoiceController}.
 *
 * @author Cursor Agent
 */
@WebMvcTest(InvoiceController.class)
@DisabledInNativeImage
@DisabledInAotMode
class InvoiceControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Owner ownerWithVisits;

	private Owner ownerWithoutVisits;

	private Pet latestPet;

	private Visit latestVisit;

	@BeforeEach
	void setup() {
		ownerWithVisits = new Owner();
		ownerWithVisits.setId(TEST_OWNER_ID);
		ownerWithVisits.setFirstName("Jean");
		ownerWithVisits.setLastName("Coleman");
		ownerWithVisits.setAddress("105 N. Lake St.");
		ownerWithVisits.setCity("Monona");
		ownerWithVisits.setTelephone("6085552654");

		PetType cat = new PetType();
		cat.setName("cat");

		Pet olderPet = new Pet();
		olderPet.setName("Samantha");
		olderPet.setBirthDate(LocalDate.of(2012, 9, 4));
		olderPet.setType(cat);
		ownerWithVisits.addPet(olderPet);
		olderPet.setId(7);

		Visit olderVisit = new Visit();
		olderVisit.setId(1);
		olderVisit.setDate(LocalDate.of(2013, 1, 1));
		olderVisit.setDescription("rabies shot");
		olderPet.addVisit(olderVisit);

		latestPet = new Pet();
		latestPet.setName("Max");
		latestPet.setBirthDate(LocalDate.of(2012, 9, 4));
		latestPet.setType(cat);
		ownerWithVisits.addPet(latestPet);
		latestPet.setId(8);

		Visit midVisit = new Visit();
		midVisit.setId(2);
		midVisit.setDate(LocalDate.of(2013, 1, 2));
		midVisit.setDescription("rabies shot");
		latestPet.addVisit(midVisit);

		latestVisit = new Visit();
		latestVisit.setId(3);
		latestVisit.setDate(LocalDate.of(2013, 1, 3));
		latestVisit.setDescription("neutered");
		latestPet.addVisit(latestVisit);

		ownerWithoutVisits = new Owner();
		ownerWithoutVisits.setId(2);
		ownerWithoutVisits.setFirstName("George");
		ownerWithoutVisits.setLastName("Franklin");
		ownerWithoutVisits.setAddress("110 W. Liberty St.");
		ownerWithoutVisits.setCity("Madison");
		ownerWithoutVisits.setTelephone("6085551023");
		Pet leo = new Pet();
		leo.setName("Leo");
		leo.setBirthDate(LocalDate.of(2010, 9, 7));
		leo.setType(cat);
		ownerWithoutVisits.addPet(leo);
		leo.setId(1);

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(ownerWithVisits));
		given(this.owners.findById(2)).willReturn(Optional.of(ownerWithoutVisits));
		given(this.owners.findById(999)).willReturn(Optional.empty());
	}

	@Test
	void generateInvoiceShowsLatestVisit() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/invoice", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/invoice"))
			.andExpect(model().attribute("owner", is(ownerWithVisits)))
			.andExpect(model().attribute("pet", is(latestPet)))
			.andExpect(model().attribute("visit", is(latestVisit)))
			.andExpect(model().attribute("consultationFee", is(InvoiceController.CONSULTATION_FEE)))
			.andExpect(model().attribute("total", is(InvoiceController.CONSULTATION_FEE)));
	}

	@Test
	void generateInvoiceRedirectsWhenOwnerHasNoVisits() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/invoice", 2))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/2"))
			.andExpect(flash().attribute("error", "No visits available to generate an invoice."));
	}

	@Test
	void generateInvoiceThrowsWhenOwnerMissing() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/invoice", 999))
			.andExpect(result -> {
				Exception resolved = result.getResolvedException();
				if (!(resolved instanceof IllegalArgumentException)
						|| !resolved.getMessage().contains("Owner not found")) {
					throw new AssertionError("Expected IllegalArgumentException for missing owner, got: " + resolved);
				}
			});
	}

}
