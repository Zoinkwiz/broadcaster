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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

@Slf4j
@Singleton
public class SkillAchievements
{
	HashMap<Skill, Integer> currentLevels = new HashMap<>();
	HashMap<Skill, Integer> currentExperience = new HashMap<>();
	int currentTotalLevel = 0;

	Client client;
	AblyConnection ablyConnection;

	@Inject
	public SkillAchievements(Client client, AblyConnection ablyConnection)
	{
		this.client = client;
		this.ablyConnection = ablyConnection;
	}

	public void setupCurrentLevels()
	{
		Skill[] skills = Skill.values();
		for (Skill skill : skills)
		{
			currentLevels.put(skill, 0);
			currentExperience.put(skill, 0);
		}
		currentTotalLevel = 0;
	}

	public void onStatChanged(StatChanged statChanged)
	{
		Skill skillChanged = statChanged.getSkill();

		// If we've just logged in so skill is 0...
		if (currentLevels.get(skillChanged) == 0)
		{
			currentLevels.put(skillChanged, statChanged.getLevel());
			currentExperience.put(skillChanged, statChanged.getXp());
			currentTotalLevel = client.getTotalLevel();
			return;
		}

		if (Experience.MAX_REAL_LEVEL <= statChanged.getLevel() && Experience.MAX_REAL_LEVEL > currentLevels.get(skillChanged))
		{
			ablyConnection.publishMessage("Has achieved level 99 in " + skillChanged.getName() + "!");
			int totalLevel = client.getTotalLevel();
			if (currentTotalLevel != 0 && totalLevel > 99 * currentExperience.size() && totalLevel > currentTotalLevel)
			{
				ablyConnection.publishMessage("Has achieved level 99 in all skills!");
			}
		}

		if (Experience.MAX_REAL_LEVEL <= statChanged.getXp())
		{
			ablyConnection.publishMessage("Has achieved 200m exp in " + skillChanged.getName() + "!");
		}

		currentLevels.put(skillChanged, statChanged.getLevel());
		currentExperience.put(skillChanged, statChanged.getXp());
		currentTotalLevel = client.getTotalLevel();
	}
}
