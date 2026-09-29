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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerTests {

	@Test
	void addPetAddsPersistedPet() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		pet.setId(5);
		pet.setName("Buddy");

		owner.addPet(pet);

		assertTrue(owner.getPets().contains(pet));
		assertEquals(1, owner.getPets().size());
	}

	@Test
	void addPetDoesNotAddDuplicatePet() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		pet.setId(5);
		pet.setName("Buddy");

		owner.addPet(pet);
		owner.addPet(pet);

		assertEquals(1, owner.getPets().size());
	}

	@Test
	void findLatestVisitReturnsEmptyWhenNoVisits() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		pet.setId(1);
		pet.setName("Leo");
		owner.addPet(pet);

		assertTrue(owner.findLatestVisit().isEmpty());
	}

	@Test
	void findLatestVisitPrefersLaterDateThenHigherId() {
		Owner owner = new Owner();

		Pet firstPet = new Pet();
		firstPet.setId(1);
		firstPet.setName("Samantha");
		owner.addPet(firstPet);

		Visit older = new Visit();
		older.setId(1);
		older.setDate(LocalDate.of(2013, 1, 1));
		older.setDescription("rabies shot");
		firstPet.addVisit(older);

		Pet secondPet = new Pet();
		secondPet.setId(2);
		secondPet.setName("Max");
		owner.addPet(secondPet);

		Visit sameDayLowerId = new Visit();
		sameDayLowerId.setId(2);
		sameDayLowerId.setDate(LocalDate.of(2013, 1, 3));
		sameDayLowerId.setDescription("rabies shot");
		secondPet.addVisit(sameDayLowerId);

		Visit sameDayHigherId = new Visit();
		sameDayHigherId.setId(3);
		sameDayHigherId.setDate(LocalDate.of(2013, 1, 3));
		sameDayHigherId.setDescription("neutered");
		secondPet.addVisit(sameDayHigherId);

		Optional<Owner.PetVisit> latest = owner.findLatestVisit();
		assertTrue(latest.isPresent());
		assertEquals(secondPet, latest.get().pet());
		assertEquals(sameDayHigherId, latest.get().visit());
	}

}
