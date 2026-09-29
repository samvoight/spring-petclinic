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

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Generates an on-screen HTML invoice for an owner's most recent visit.
 *
 * @author Cursor Agent
 */
@Controller
class InvoiceController {

	static final BigDecimal CONSULTATION_FEE = new BigDecimal("50.00");

	private final OwnerRepository owners;

	public InvoiceController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping("/owners/{ownerId}/invoice")
	public String generateInvoice(@PathVariable("ownerId") int ownerId, Model model,
			RedirectAttributes redirectAttributes) {
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
					+ ". Please ensure the ID is correct and the owner exists in the database."));

		Optional<Owner.PetVisit> latest = owner.findLatestVisit();
		if (latest.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "No visits available to generate an invoice.");
			return "redirect:/owners/{ownerId}";
		}

		Owner.PetVisit petVisit = latest.get();
		model.addAttribute("owner", owner);
		model.addAttribute("pet", petVisit.pet());
		model.addAttribute("visit", petVisit.visit());
		model.addAttribute("consultationFee", CONSULTATION_FEE);
		model.addAttribute("total", CONSULTATION_FEE);
		return "owners/invoice";
	}

}
