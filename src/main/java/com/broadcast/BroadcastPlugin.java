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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Provides;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.CompletionListener;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.WorldType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
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
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private BroadcastConfig config;

	private AblyRealtime ablyRealtime;
	private Channel ablyClanChannel;

	private final String CHANNEL_NAME_PREFIX = "broadcast";

	private static final String PET_FOLLOWING_MESSAGE = "You have a funny feeling like you're being followed";
	private static final String PET_INVENTORY_MESSAGE = "You feel something weird sneaking into your backpack";
	private static final String PET_POTENTIAL_MESSAGE = "You have a funny feeling like you would have been followed";

	private String VALID_REGEX_TEXT;

	@Override
	protected void startUp() throws Exception
	{
		ablyRealtime = new AblyRealtime(config.apiKey());
		ablyClanChannel = ablyRealtime.channels.get(CHANNEL_NAME_PREFIX);
		StringBuilder regex = new StringBuilder("(Rolled to receive a pet!)|(Received the (test");
		for (Pet pet : Pet.values())
		{
			regex.append("|").append(pet.getName());
		}
		regex.append(") pet!)");
		VALID_REGEX_TEXT = regex.toString();

		setupBroadcasts();
	}

	@Override
	protected void shutDown()
	{
		ablyRealtime.connection.close();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("broadcast"))
		{
			if (event.getKey().equals("clanBroadcastReceive"))
			{
				setupBroadcasts();
			}
		}

		if (event.getKey().equals("apiKey"))
		{
			ablyRealtime.connection.close();
			try
			{
				ablyRealtime = new AblyRealtime(config.apiKey());
				ablyClanChannel.unsubscribe();
				ablyClanChannel = ablyRealtime.channels.get(CHANNEL_NAME_PREFIX);
				setupBroadcasts();
			}
			catch (AblyException ignored)
			{

			}
		}
	}

	public void publishMessage(String notification)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		try
		{
			JsonObject msg = io.ably.lib.util.JsonUtils.object()
				.add("username", getAccountIcon() + client.getLocalPlayer().getName())
				.add("notification", notification).toJson();
			if (config.clanBroadcast())
			{
				ablyClanChannel.publish("event", msg, new CompletionListener()
				{
					@Override
					public void onSuccess()
					{
						System.out.println("Message successfully sent");
					}

					@Override
					public void onError(ErrorInfo reason)
					{
						System.err.println("Unable to publish message; err = " + reason.message);
					}
				});
			}
		}
		catch (AblyException err)
		{
			System.out.println(err.getMessage());
		}
	}

	private void handleMessage(Message message)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			try
			{
				clientThread.invokeLater(() -> {
					Gson gson = new Gson();
					BroadcastMessage msg = gson.fromJson((JsonElement) message.data, BroadcastMessage.class);
					if (msg.notification.matches(VALID_REGEX_TEXT) && msg.username.length() <= 20)
					{
						client.addChatMessage(ChatMessageType.FRIENDSCHAT, msg.username, msg.notification,
							config.groupName() + " Broadcast");
					}
				});
			}
			catch(ClassCastException ignored)
			{
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE
			&& event.getType() != ChatMessageType.ENGINE
			&& event.getType() != ChatMessageType.SPAM
			&& event.getType() != ChatMessageType.TRADE
			&& event.getType() != ChatMessageType.FRIENDSCHATNOTIFICATION)
		{
			return;
		}

		String chatMessage = event.getMessage();
		if (chatMessage.contains(PET_POTENTIAL_MESSAGE))
		{
			publishMessage("Rolled to receive a pet!");
		}
		else if (chatMessage.contains(PET_FOLLOWING_MESSAGE))
		{
			publishPetFollowingMessage();
		}
		else if (chatMessage.contains(PET_INVENTORY_MESSAGE))
		{
			publishPetInventoryMessage();
		}
	}
	private void publishPetFollowingMessage()
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
				publishMessage("Received the " + matchingPet.getName() + " pet!");
				return;
			}
		}
	}

	private String getAccountIcon()
	{
		switch (client.getAccountType())
		{
			case IRONMAN:
				return "<img=2>";
			case HARDCORE_IRONMAN:
				return "<img=10>";
			case ULTIMATE_IRONMAN:
				return "<img=3>";
		}
		if (client.getWorldType().contains( WorldType.LEAGUE))
		{
			return "<img=33>";
		}

		return "";
	}

	private void publishPetInventoryMessage()
	{
		Item[] items = Objects.requireNonNull(client.getItemContainer(InventoryID.INVENTORY)).getItems();
		for (Item item : items)
		{
			InventoryPet pet = InventoryPet.findPet(item.getId());
			if (pet != null)
			{
				publishMessage("Receive the " + pet.getName() + " pet!");
				return;
			}
		}
	}

	@Provides
	BroadcastConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BroadcastConfig.class);
	}

	private void setupBroadcasts()
	{
		try
		{
			ablyClanChannel.unsubscribe();
			if (config.clanBroadcastReceive())
			{
				ablyClanChannel.subscribe((Channel.MessageListener) this::handleMessage);
			}
			else
			{
			}
		}
		catch (AblyException e)
		{
			e.printStackTrace();
		}
	}
}
