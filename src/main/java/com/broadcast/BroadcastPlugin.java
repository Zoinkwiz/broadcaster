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

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Broadcasts",
	description = "Allows broadcasting to clan chat of pet drops",
	tags = { "broadcast" }
)
public class BroadcastPlugin extends Plugin
{
	@Inject
	private AblyConnection ablyConnection;

	@Inject
	private PetDrops petDrops;

	@Inject
	private SkillAchievements skillAchievements;

	private static final String PET_FOLLOWING_MESSAGE = "You have a funny feeling like you're being followed";
	private static final String PET_INVENTORY_MESSAGE = "You feel something weird sneaking into your backpack";
	private static final String PET_POTENTIAL_MESSAGE = "You have a funny feeling like you would have been followed";

	@Override
	protected void startUp() throws Exception
	{
		ablyConnection.startConnection();
	}

	@Override
	protected void shutDown()
	{

		ablyConnection.closeConnection();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("broadcast"))
		{
			if (event.getKey().equals("globalBroadcastReceive"))
			{
				ablyConnection.setupAblyGlobalSubscription();
			}
			if (event.getKey().equals("groupBroadcastReceive"))
			{
				ablyConnection.setupAblyPrivateSubscription();
			}
			if (event.getKey().equals("apiKey"))
			{
				ablyConnection.connectToNewAblyAccountPrivate();
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		skillAchievements.onStatChanged(statChanged);
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		final GameState state = event.getGameState();

		if (state == GameState.LOGGED_IN)
		{
			skillAchievements.setupCurrentLevels();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE
			&& event.getType() != ChatMessageType.ENGINE)
		{
			return;
		}

		String chatMessage = event.getMessage();
		if (chatMessage.contains(PET_POTENTIAL_MESSAGE))
		{
			ablyConnection.publishMessage("Rolled to receive a pet!");
		}
		else if (chatMessage.contains(PET_FOLLOWING_MESSAGE))
		{
			petDrops.publishPetFollowingMessage();
		}
		else if (chatMessage.contains(PET_INVENTORY_MESSAGE))
		{
			petDrops.publishPetInventoryMessage();
		}
	}

	@Provides
	BroadcastConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BroadcastConfig.class);
	}
}
