package com.broadcast;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.ui.JagexColors;

@ConfigGroup("broadcast")
public interface BroadcastConfig extends Config
{
	@ConfigItem(
		keyName = "apiKey",
		name = "Ably API key",
		description = "Key is used to connect you to the appropriate messages"
	)
	default String apiKey()
	{
		return "FJ0Xqw.k0vt4Q:_SMrRHML8qEIqm5s";
	}

	@ConfigItem(
		keyName = "groupName",
		name = "Name of group",
		description = "Choose a name to identify this group"
	)
	default String groupName()
	{
		return "Global";
	}
	@ConfigItem(
		keyName = "clanBroadcast",
		name = "Send broadcasts",
		description = "Broadcast drops you get to the group"
	)
	default boolean clanBroadcast()
	{
		return true;
	}

	@ConfigItem(
		keyName = "clanBroadcastReceive",
		name = "Receive broadcasts",
		description = "Receive broadcast from players in your current clan chat"
	)
	default boolean clanBroadcastReceive()
	{
		return true;
	}

	@ConfigItem(
		keyName = "broadcastColour",
		name = "Broadcast colour",
		description = "The colour the broadcast should appear in"
	)
	default Color broadcastColour()
	{
		return JagexColors.CHAT_FC_TEXT_OPAQUE_BACKGROUND;
	}
}
