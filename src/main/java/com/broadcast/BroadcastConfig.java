package com.broadcast;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.ui.JagexColors;

@ConfigGroup("broadcast")
public interface BroadcastConfig extends Config
{
	@ConfigSection(
		position = 1,
		name = "Global broadcasts",
		description = "Options for global broadcasts"
	)
	String globalBroadcastsSection = "globalBroadcastsSection";

	@ConfigItem(
		keyName = "globalBroadcastSend",
		name = "Send global broadcasts",
		description = "Broadcast drops you get to the global group",
		section = globalBroadcastsSection
	)
	default boolean globalBroadcastSend()
	{
		return true;
	}

	@ConfigItem(
		keyName = "globalBroadcastReceive",
		name = "Receive global broadcasts",
		description = "Receive broadcast from the global group",
		section = globalBroadcastsSection
	)
	default boolean globalBroadcastReceive()
	{
		return true;
	}

	@ConfigItem(
		keyName = "globalBroadcastColour",
		name = "Global broadcast colour",
		description = "The colour the global broadcasts should appear in",
		section = globalBroadcastsSection
	)
	default Color globalBroadcastColour()
	{
		return JagexColors.CHAT_FC_TEXT_OPAQUE_BACKGROUND;
	}

	// Group
	@ConfigSection(
		position = 2,
		name = "Private group broadcasts",
		closedByDefault = true,
		description = "Options for private broadcasts"
	)
	String groupBroadcastsSection = "groupBroadcastsSection";

	@ConfigItem(
		keyName = "apiKey",
		name = "Ably API key",
		description = "Put a key here to use a private group's broadcasts",
		section = groupBroadcastsSection
	)
	default String apiKey()
	{
		return "";
	}

	@ConfigItem(
		keyName = "groupName",
		name = "Name of group",
		description = "Choose a name to identify your group",
		section = groupBroadcastsSection
	)
	default String groupName()
	{
		return "Clan";
	}

	@ConfigItem(
		keyName = "groupBroadcastSend",
		name = "Send group broadcasts",
		description = "Broadcast drops you get to your private group",
		section = groupBroadcastsSection
	)
	default boolean groupBroadcastSend()
	{
		return true;
	}

	@ConfigItem(
		keyName = "groupBroadcastReceive",
		name = "Receive group broadcasts",
		description = "Receive broadcast from the private group",
		section = groupBroadcastsSection
	)
	default boolean groupBroadcastReceive()
	{
		return true;
	}

	@ConfigItem(
		keyName = "groupBroadcastColour",
		name = "Group broadcast colour",
		description = "The colour the private group broadcasts should appear in",
		section = groupBroadcastsSection
	)
	default Color groupBroadcastColour()
	{
		return JagexColors.CHAT_FC_TEXT_OPAQUE_BACKGROUND;
	}
}
