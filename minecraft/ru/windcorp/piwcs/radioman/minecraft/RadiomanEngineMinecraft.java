package ru.windcorp.piwcs.radioman.minecraft;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static ru.windcorp.piwcs.radioman.RadiomanIntercoms.*;

public class RadiomanEngineMinecraft implements Listener {
	
	private static final Queue<String> BROADCAST_QUEUE = new ConcurrentLinkedQueue<>();
	
	public static void broadcastToMinecraft(String text) {
		BROADCAST_QUEUE.add(text);
	}
	
	public static void doBroadcasts() {
		while (!BROADCAST_QUEUE.isEmpty()) Bukkit.broadcastMessage(BROADCAST_QUEUE.poll());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		if (isOpen()) {
			sendRequest(TYPE_MINECRAFT_CHAT, input -> {
				input.writeUTF(ChatColor.stripColor(e.getPlayer().getDisplayName()));
				input.writeUTF(e.getMessage());
			});
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoined(PlayerJoinEvent e) {
		onPlayerlistUpdate();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuits(PlayerQuitEvent e) {
		onPlayerlistUpdate();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKicked(PlayerKickEvent e) {
		onPlayerlistUpdate();
	}
	
	public void onPlayerlistUpdate() {
		if (isOpen()) {
			sendPlayerlist();
		}
	}
	
	public void sendPlayerlist() {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		
		sendRequest(TYPE_MINECRAFT_PLAYERLIST, input -> {
			input.writeByte(players.size());
			
			for (Player player : players) {
				input.writeUTF(player.getName());
				input.writeUTF(ChatColor.stripColor(RadiomanPlugin.getDisplayName(player)));
			}
		});
	}

}
