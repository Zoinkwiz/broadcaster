/*
 * Copyright (c) 2021 by Zoinkwiz
 * Copyright (c) 2020 by micro
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
 *
 * Portions of the code are based off of the "Implings" RuneLite plugin.
 * The "Implings" is:
 * Copyright (c) 2017, Robin <robin.weymans@gmail.com>
 * All rights reserved.
 */
package com.broadcast;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;

@AllArgsConstructor
@Getter
public enum InventoryPet
{
	ABYSSAL_ORPHAN("Abyssal orphan", ItemID.ABYSSAL_ORPHAN),
	BABY_CHINCHOMPA("Baby chinchompa", ItemID.BABY_CHINCHOMPA, ItemID.BABY_CHINCHOMPA_13324,
		ItemID.BABY_CHINCHOMPA_13325, ItemID.BABY_CHINCHOMPA_13326),
	BABY_MOLE("Baby mole", ItemID.BABY_MOLE),
	BEAVER("Beaver", ItemID.BEAVER),
	BLOODHOUND("Bloodhound", ItemID.BLOODHOUND),
	CALLISTO_CUB("Callisto cub", ItemID.CALLISTO_CUB),
	CHAOS_ELEMENTAL_JR("Chaos elemental jr.", ItemID.CHAOS_ELEMENTAL, ItemID.PET_CHAOS_ELEMENTAL),
	CHOMPY_CHICK("Chompy chick", ItemID.CHOMPY_CHICK),
	CORPOREAL_CRITTER("Corporeal critter", ItemID.PET_CORPOREAL_CRITTER),
	CORRUPTED_YOUNGLLEF("Corrupted youngllef", ItemID.CORRUPTED_YOUNGLLEF),
	DAGANNOTH_PRIME_JR("Dagannoth prime jr.", ItemID.PET_DAGANNOTH_PRIME),
	DAGANNOTH_REX_JR("Dagannoth rex jr.", ItemID.PET_DAGANNOTH_REX),
	DAGANNOTH_SUPREME_JR("Dagannoth supreme jr.", ItemID.PET_DAGANNOTH_SUPREME),
	DARK_CORE("Dark core", ItemID.PET_DARK_CORE),
	GENERAL_GRAARDOR_JR("General graardor jr.", ItemID.PET_GENERAL_GRAARDOR),
	GIANT_SQUIRREL("Giant squirrel", ItemID.GIANT_SQUIRREL),
	HELLPUPPY("Hellpuppy", ItemID.HELLPUPPY),
	HERBI("Herbi", ItemID.HERBI),
	HERON("Heron", ItemID.HERON),
	IKKLE_HYDRA("Ikkle hydra", ItemID.IKKLE_HYDRA, ItemID.IKKLE_HYDRA_22748, ItemID.IKKLE_HYDRA_22750,
		ItemID.IKKLE_HYDRA_22752),
	JALNIBREK("Jal-nib-brek", ItemID.JALNIBREK),
	KALPHITE_PRINCESS("Kalphite princess", ItemID.KALPHITE_PRINCESS, ItemID.KALPHITE_PRINCESS_12654),
	KRAKEN_6640("Kraken", ItemID.PET_KRAKEN),
	KREEARRA_JR("Kree'arra jr.", ItemID.KREEARRA, ItemID.PET_KREEARRA),
	KRIL_TSUTSAROTH_JR("K'ril Tsutsaroth jr.", ItemID.PET_KRIL_TSUTSAROTH),
	LIL_CREATOR("Soul Wars pet", ItemID.LIL_CREATOR, ItemID.LIL_DESTRUCTOR),
	LIL_ZIK("Lil' zik", ItemID.LIL_ZIK),	// Seen in game other player
	LITTLE_NIGHTMARE("Little nightmare", ItemID.LITTLE_NIGHTMARE),
	MIDNIGHT("Midnight", ItemID.MIDNIGHT),
	NOON("Noon", ItemID.NOON),
	OLMLET("Olmlet", ItemID.OLMLET),
	PENANCE_PET("Penance pet", NpcID.PENANCE_PET, NpcID.PENANCE_PET_6674),
	PHOENIX("Phoenix", ItemID.PHOENIX, ItemID.PHOENIX_24483, ItemID.PHOENIX_24484, ItemID.PHOENIX_24485,
		ItemID.PHOENIX_24486),
	PRINCE_BLACK_DRAGON("Prince Black Dragon", ItemID.PRINCE_BLACK_DRAGON),
	RIFT_GUARDIAN("Rift Guardian", ItemID.RIFT_GUARDIAN, ItemID.RIFT_GUARDIAN_20667,
		ItemID.RIFT_GUARDIAN_20669, ItemID.RIFT_GUARDIAN_20671, ItemID.RIFT_GUARDIAN_20673,
		ItemID.RIFT_GUARDIAN_20675, ItemID.RIFT_GUARDIAN_20677, ItemID.RIFT_GUARDIAN_20679,
		ItemID.RIFT_GUARDIAN_20681, ItemID.RIFT_GUARDIAN_20683, ItemID.RIFT_GUARDIAN_20685,
		ItemID.RIFT_GUARDIAN_20687, ItemID.RIFT_GUARDIAN_20689, ItemID.RIFT_GUARDIAN_20691
	),
	ROCK_GOLEM("Rock golem", ItemID.ROCK_GOLEM),	// Rock
	ROCKY("Rocky", ItemID.ROCKY),
	SCORPIAS_OFFSPRING("Scorpia's offspring", ItemID.SCORPIAS_OFFSPRING),
	SKOTOS("Skotos", ItemID.SKOTOS),
	SMOKE_DEVIL("Smoke devil", ItemID.PET_SMOKE_DEVIL, ItemID.PET_SMOKE_DEVIL_22663),
	SMOLCANO("Smolcano", ItemID.SMOLCANO),
	SNAKELING("Snakeling", ItemID.PET_SNAKELING, ItemID.PET_SNAKELING_12939, ItemID.PET_SNAKELING_12940),
	SRARACHA("Sraracha", ItemID.SRARACHA),
	TANGLEROOT("Tangleroot", ItemID.TANGLEROOT, ItemID.TANGLEROOT_24555, ItemID.TANGLEROOT_24557,
		ItemID.TANGLEROOT_24559, ItemID.TANGLEROOT_24561, ItemID.TANGLEROOT_24563),
	TINY_TEMPOR("Tiny Tempor", ItemID.TINY_TEMPOR),
	TZREKJAD("Tzrek-jad", ItemID.TZREKJAD),
	VENENATIS_SPIDERLING("Venentis spiderling", ItemID.VENENATIS_SPIDERLING),
	VETION_JR("Vetion Jr.",ItemID.VETION_JR, ItemID.VETION_JR_13180),
	VORKI("Vorki", ItemID.VORKI),	// Seen in game, other player
	YOUNGLLEF("Youngllef", ItemID.YOUNGLLEF),
	ZILYANA_JR("Zilyana Jr.", ItemID.PET_ZILYANA);

	private final List<Integer> npcIds;
	private final String name;

	InventoryPet(String name, Integer... npcIds)
	{
		this.name = name;
		this.npcIds = Arrays.asList(npcIds);
	}

	private static final Map<Integer, InventoryPet> PETS;

	static
	{
		ImmutableMap.Builder<Integer, InventoryPet> builder = new ImmutableMap.Builder<>();

		for (InventoryPet pet : values())
		{
			pet.npcIds.forEach((npcId) -> builder.put(npcId, pet));
		}

		PETS = builder.build();
	}

	/**
	 * Returns the Pet enum if the passed NPCid is a pet, null if not
	 */
	static InventoryPet findPet(int npcId)
	{
		return PETS.get(npcId);
	}
}

