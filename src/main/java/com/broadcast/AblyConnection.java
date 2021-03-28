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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

@Slf4j
@Singleton
public class AblyConnection
{
	private final Client client;

	private final HashMap<String, ArrayList<String>> previousMessages = new HashMap<>();

	private final String DEFAULT_KEY = "FJ0Xqw.k0vt4Q:_SMrRHML8qEIqm5s";
	// Publish key is only used for global app due to additional filtering functionality
	private final String DEFAULT_PUBLISH_KEY = "FJ0Xqw.AOiULQ:PZOrlbospQr4h90h";

	@Inject
	ChatMessageManager chatMessageManager;

	private AblyRealtime ablyRealtimePublish;
	private AblyRealtime ablyRealtimeSubscribe;
	private Channel ablyClanChannelSubscribe;
	private Channel ablyClanChannelPublish;
	private final BroadcastConfig config;

	private final String VALID_SYMBOL_TEXT = "<img=(33|2|10|3)>";
	private final String VALID_USERNAME = "[a-zA-Z\\d- ]{1,12}";
	private String VALID_REGEX_PET_TEXT;
	private String VALID_SKILL_TEXT;

	@Inject
	public AblyConnection(Client client, BroadcastConfig config)
	{
		this.client = client;
		this.config = config;
		createPetRegex();
		createSkillRegex();
	}

	public void startConnection()
	{
		setupAblyInstance();
		setupChannels();
		setupAblySubscriptions();
	}

	public void closeConnection()
	{
		ablyRealtimePublish.connection.close();
		ablyRealtimeSubscribe.connection.close();
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
				.add("symbol", getAccountIcon())
				.add("username", client.getLocalPlayer().getName())
				.add("notification", notification).toJson();
			if (config.clanBroadcast())
			{
				ablyClanChannelPublish.publish("event", msg);
			}
		}
		catch (AblyException err)
		{
			System.out.println(err.getMessage());
		}
	}

	private void handleAblyMessage(Message message)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			try
			{
				Gson gson = new Gson();
				BroadcastMessage msg = gson.fromJson((JsonElement) message.data, BroadcastMessage.class);

				previousMessages.computeIfAbsent(msg.username, k -> new ArrayList<>());
				// If someone is manually spamming the same message during a session, block it
				if (previousMessages.get(msg.username).contains(msg.notification))
				{
					return;
				}
				previousMessages.get(msg.username).add(msg.notification);

				final ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder()
					.append(config.broadcastColour(), msg.notification);

				if (msg.username.length() > 12 || !msg.username.matches(VALID_USERNAME))
				{
					return;
				}

				if (!msg.symbol.matches(VALID_SYMBOL_TEXT) && !msg.symbol.equals(""))
				{
					return;
				}

				if (!msg.notification.matches(VALID_REGEX_PET_TEXT) &&
					!msg.notification.matches(VALID_SKILL_TEXT))
				{
					return;
				}

				chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.FRIENDSCHAT)
					.name(msg.symbol + msg.username)
					.sender(config.groupName() + " Broadcast")
					.runeLiteFormattedMessage(chatMessageBuilder.build())
					.build());

			}
			catch (ClassCastException ignored)
			{
			}
		}
	}

	public void connectToNewAblyAccount()
	{
		ablyRealtimePublish.connection.close();
		ablyRealtimeSubscribe.connection.close();

		setupAblyInstance();
		setupChannels();
		setupAblySubscriptions();
	}

	private void setupAblyInstance()
	{
		try
		{
			ablyRealtimeSubscribe = new AblyRealtime(config.apiKey());
			if (DEFAULT_KEY.equals(config.apiKey()))
			{
				ablyRealtimePublish = new AblyRealtime(DEFAULT_PUBLISH_KEY);
			}
			else
			{
				ablyRealtimePublish = new AblyRealtime(config.apiKey());
			}
		} catch(AblyException ignored) {}
	}

	public void setupChannels()
	{
		String CHANNEL_NAME_PREFIX = "broadcast";

		if (config.apiKey().equals(DEFAULT_KEY))
		{
			ablyClanChannelPublish = ablyRealtimePublish.channels.get(CHANNEL_NAME_PREFIX + ":publish");
			ablyClanChannelSubscribe = ablyRealtimeSubscribe.channels.get(CHANNEL_NAME_PREFIX + ":subscribe");
		}
		else
		{
			ablyClanChannelPublish = ablyRealtimePublish.channels.get(CHANNEL_NAME_PREFIX);
			ablyClanChannelSubscribe = ablyRealtimeSubscribe.channels.get(CHANNEL_NAME_PREFIX);
		}
	}

	public void setupAblySubscriptions()
	{
		try
		{
			ablyClanChannelSubscribe.unsubscribe();
			if (config.clanBroadcastReceive())
			{
				ablyClanChannelSubscribe.subscribe((Channel.MessageListener) this::handleAblyMessage);
			}
		}
		catch (AblyException e)
		{
			e.printStackTrace();
		}
	}

	private void createPetRegex()
	{
		StringBuilder regex = new StringBuilder("(Rolled to receive a pet!)|(Received the (test");
		for (Pet pet : Pet.values())
		{
			regex.append("|").append(pet.getName());
		}
		regex.append(") pet!)");
		VALID_REGEX_PET_TEXT = regex.toString();
	}

	private void createSkillRegex()
	{
		StringBuilder regex = new StringBuilder("Has achieved (level (\\d{1,2})|200m exp) in (all skills");
		for (Skill skill : Skill.values())
		{
			regex.append("|").append(skill.getName());
		}
		regex.append(")!");
		VALID_SKILL_TEXT = regex.toString();
	}

	private String getAccountIcon()
	{
		if (client.getWorldType().contains( WorldType.LEAGUE))
		{
			return "<img=33>";
		}
		switch (client.getAccountType())
		{
			case IRONMAN:
				return "<img=2>";
			case HARDCORE_IRONMAN:
				return "<img=10>";
			case ULTIMATE_IRONMAN:
				return "<img=3>";
		}

		return "";
	}
}
