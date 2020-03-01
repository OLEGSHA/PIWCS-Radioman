package ru.windcorp.piwcs.radioman.telegram;

import org.telegram.telegrambots.meta.api.objects.Message;

public class LogMessage {
	
	private Message message = null;
	
	public synchronized void invalidate() {
		message = null;
	}
	
	public synchronized void append(String text) {
		if (message == null) {
			RadiomanEngineTelegram.send(text);
		}
	}

}
