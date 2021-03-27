/*
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.broadcast;

import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.NPC;

@Slf4j
@Singleton
public class PetDrops
{
	@Inject
	AblyConnection ablyConnection;

	@Inject
	Client client;

	@Inject
	public PetDrops(AblyConnection ablyConnection, Client client)
	{
		this.ablyConnection = ablyConnection;
		this.client = client;
	}

	void publishPetInventoryMessage()
	{
		Item[] items = Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).getItems();
		for (Item item : items)
		{
			InventoryPet pet = InventoryPet.findPet(item.getId());
			if (pet != null)
			{
				ablyConnection.publishMessage("Received the " + pet.getName() + " pet!");
				return;
			}
		}
	}

	void publishPetFollowingMessage()
	{
		List<NPC> match = client.getNpcs().stream()
			.filter(npc -> npc.getInteracting() != null)
			.filter(npc -> npc.getInteracting() == client.getLocalPlayer())
			.collect(Collectors.toList());

		for (NPC npc : match)
		{
			Pet matchingPet = Pet.findPet(npc.getId());
			if (matchingPet != null)
			{
				ablyConnection.publishMessage("Received the " + matchingPet.getName() + " pet!");
				return;
			}
		}
	}
}
