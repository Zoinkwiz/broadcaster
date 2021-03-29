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
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.Message;
import java.awt.Color;
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

	private final String CHANNEL_NAME_PREFIX = "broadcast";

	@Inject
	ChatMessageManager chatMessageManager;

	private final BroadcastConfig config;

	// Global broadcasts
	private AblyRealtime ablyGlobalRealtime;
	private Channel ablyGlobalChannelSubscribe;
	private Channel ablyGlobalChannelPublish;

	// Private broadcasts
	private AblyRealtime ablyPrivateRealtime;
	private Channel ablyPrivateChannel;

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
		setupAblyInstances();
		setupChannels();
		setupAblySubscriptions();
	}

	public void closeConnection()
	{
		ablyGlobalRealtime.connection.close();
		ablyPrivateRealtime.connection.close();
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
			if (config.globalBroadcastSend())
			{
				ablyGlobalChannelPublish.publish("event", msg);
			}
			if (config.groupBroadcastSend())
			{
				ablyPrivateChannel.publish("event", msg);
			}
		}
		catch (AblyException err)
		{
			System.out.println(err.getMessage());
		}
	}

	private void handleGlobalMessage(Message message)
	{
		handleAblyMessage(message, config.globalBroadcastColour(), "Global");
	}

	private void handlePrivateMessage(Message message)
	{
		handleAblyMessage(message, config.groupBroadcastColour(), config.groupName());
	}

	private void handleAblyMessage(Message message, Color color, String broadcastName)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
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
				.append(color, msg.notification);

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
				.sender(broadcastName + " Broadcast")
				.runeLiteFormattedMessage(chatMessageBuilder.build())
				.build());

		}
	}


	public void connectToNewAblyAccountPrivate()
	{
		ablyPrivateRealtime.connection.close();

		setupAblyPrivateInstance();
		setupPrivateChannel();
		setupAblyPrivateSubscription();
	}

	private void setupAblyInstances()
	{
		setupAblyGlobalInstances();
		setupAblyPrivateInstance();
	}

	private void setupAblyGlobalInstances()
	{
		try
		{
			ClientOptions clientOptions = new ClientOptions();
			clientOptions.authUrl = "https://runelite-broadcaster.herokuapp.com/token";
			ablyGlobalRealtime = new AblyRealtime(clientOptions);
		}
		catch (AblyException e)
		{
			e.printStackTrace();
		}
	}

	private void setupAblyPrivateInstance()
	{
		try
		{
			if (!config.apiKey().equals(""))
			{
				ablyPrivateRealtime = new AblyRealtime(config.apiKey());
			}
		}
		catch (AblyException e)
		{
			e.printStackTrace();
		}
	}

	public void setupChannels()
	{
		setupGlobalChannels();
		setupPrivateChannel();
	}

	private void setupGlobalChannels()
	{
		ablyGlobalChannelPublish = ablyGlobalRealtime.channels.get(CHANNEL_NAME_PREFIX + ":publish");
		ablyGlobalChannelSubscribe = ablyGlobalRealtime.channels.get(CHANNEL_NAME_PREFIX + ":subscribe");
	}

	private void setupPrivateChannel()
	{
		if (ablyPrivateRealtime != null && !config.apiKey().equals(""))
		{
			ablyPrivateChannel = ablyPrivateRealtime.channels.get(CHANNEL_NAME_PREFIX);
		}
	}

	public void setupAblySubscriptions()
	{
		setupAblyGlobalSubscription();
		setupAblyPrivateSubscription();
	}

	public void setupAblyGlobalSubscription()
	{
		try
		{
			ablyGlobalChannelSubscribe.unsubscribe();
			if (config.globalBroadcastReceive())
			{
				ablyGlobalChannelSubscribe.subscribe((Channel.MessageListener) this::handleGlobalMessage);
			}
		}
		catch (AblyException e)
		{
			e.printStackTrace();
		}
	}

	public void setupAblyPrivateSubscription()
	{
		try
		{
			ablyPrivateChannel.unsubscribe();
			if (config.groupBroadcastReceive())
			{
				ablyPrivateChannel.subscribe((Channel.MessageListener) this::handlePrivateMessage);
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
