package ru.windcorp.piwcs.radioman.telegram;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.windcorp.piwcs.radioman.RadiomanIntercoms.*;
import static ru.windcorp.piwcs.radioman.RadiomanTemplates.tpl;

public class RadiomanRelay {
	
	private static final AtomicBoolean KEEP_RUNNING = new AtomicBoolean(true);
	
	public static void runServer(int port) throws IOException {
		System.out.println("Starting relay at port " + port);
		setCallback(RadiomanRelay::handleRequest);
		
		try (ServerSocket server = new ServerSocket(port, 1, InetAddress.getLocalHost())) {
		
			while (KEEP_RUNNING.get()) {
				try (Socket minecraft = server.accept()) {
					RadiomanEngineTelegram.onOnline();
					
					open(
							new DataInputStream(minecraft.getInputStream()),
							new DataOutputStream(minecraft.getOutputStream())
						);
				
					while (KEEP_RUNNING.get()) {
						processRequest();
					}
				
				} catch (IOException ignore) {
				} finally {
					close();
				}
				
				RadiomanEngineTelegram.onOffline();
			}
		
		}
	}

	public static void stop() {
		KEEP_RUNNING.set(false);
	}
	
	public static void handleRequest(int type, DataInput input) throws IOException {
		switch (type) {
		case TYPE_MINECRAFT_CHAT:
			RadiomanEngineTelegram.send(tpl("minecraft_message",
					input.readUTF(),
					input.readUTF()));
			RadiomanEngineTelegram.getPlayerlist().invalidate();
			break;
		case TYPE_MINECRAFT_JOINED:
			RadiomanEngineTelegram.getPlayerlist().onPlayerJoined(
					input.readUTF(),
					input.readUTF()
			);
			break;
		case TYPE_MINECRAFT_LEFT:
			RadiomanEngineTelegram.getPlayerlist().onPlayerLeft(
					input.readUTF()
			);
			break;
		case TYPE_MINECRAFT_PLAYERLIST:
			Playerlist pl = RadiomanEngineTelegram.getPlayerlist();
			
			pl.clear();
			
			int players = input.readUnsignedByte();
			for (int i = 0; i < players; ++i) {
				pl.putSilently(
						input.readUTF(),
						input.readUTF()
				);
			}
			
			break;
		}
	}
	
	public static void broadcast(String msg) {
		sendRequest(TYPE_TELEGRAM_BROADCAST, input -> input.writeUTF(msg));
	}

}
