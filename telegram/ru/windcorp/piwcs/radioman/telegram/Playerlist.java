package ru.windcorp.piwcs.radioman.telegram;

import java.util.Map;
import java.util.TreeMap;

import org.telegram.telegrambots.meta.api.objects.Message;

import static ru.windcorp.piwcs.radioman.RadiomanTemplates.tpl;

public class Playerlist {
	
	private Message message = null;
	private final Map<String, String> players = new TreeMap<>();
	
	public synchronized void onPlayerJoined(String username, String displayName) {
		if (putSilently(username, displayName)) {
			updatePlayerList();
		}
	}
	
	public synchronized void onPlayerLeft(String username) {
		if (players.remove(username) != null) {
			updatePlayerList();
		}
	}
	
	public synchronized void clear() {
		players.clear();
		updatePlayerList();
	}
	
	public synchronized int getSize() {
		return players.size();
	}
	
	public synchronized boolean putSilently(String username, String displayName) {
		return players.putIfAbsent(username, displayName) == null;
	}
	
	public synchronized void invalidate() {
		message = null;
	}
	
	private synchronized String generateText() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(tpl("telegram_playerlist_header"));
		sb.append("\n");
		
		if (players.isEmpty()) {
			sb.append(tpl("telegram_playerlist_empty"));
		} else {
			boolean needsComma = false;
			for (Map.Entry<String, String> entry : players.entrySet()) {
				if (needsComma) {
					sb.append(", ");
				} else {
					needsComma = true;
				}
				
				sb.append(entry.getValue());
			}
		}
		
		return sb.toString();
	}
	
	public synchronized void updatePlayerList() {
		if (message == null) {
			message = RadiomanEngineTelegram.send(generateText());
		} else {
			message = RadiomanEngineTelegram.edit(message, generateText());
		}
		ServerStatusDisplayer.update();
	}

}
