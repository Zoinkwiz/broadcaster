# Runelite Broadcasts

This plugin allows for players to have broadcasts to other players who also have this plugin for events that occur for them.

At present, this only supports broadcasts for pet drops and skill achievements (99s/200m).

By default, a player starts off with settings which subscribe them to global broadcasts, and result in broadcasts from them to the global channel.

<img src="https://i.imgur.com/HnHuDaX.png">

## Setting up a custom broadcast group

Perhaps you'd like to have your own group for broadcasts between friends, or just the global one is too busy. You can do this by all making use of the same App in Ably with a different API Key.

To get an API Key to share with your friends, firstly [sign up to Ably](https://ably.com/signup). Once you have an account, go to the [Default app](https://ably.com/accounts/any) created, and then the [API key tab](https://ably.com/accounts/any/apps/any/app_keys). Copy or make a new API key with at least Publish and Subscribe permissions, add it to the plugin's setting 'API key', and share it with your group!

The free offering of Ably is 3M messages a month and 500 concurrent connections, so be aware if you exceed this messages will stop being sent and received.

## How this works

This makes use of [Ably](https://www.ably.com) to easily implement a [Pub/Sub system](https://ably.com/documentation/realtime) for broadcasts. When you get a drop the plugin is configured to detect, it will send a message to a channel in Ably saying you got the drop. Anyone subscribed to updates from this channel will then receive the message over WebSockets.
