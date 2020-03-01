package ru.windcorp.piwcs.radioman.minecraft;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import ru.windcorp.piwcs.radioman.RadiomanConstants;

import static ru.windcorp.piwcs.radioman.RadiomanIntercoms.*;

public class RadiomanClient {

	private static final AtomicBoolean KEEP_RUNNING = new AtomicBoolean(true);
	
	private static Thread runningThread = null;
	
	public static synchronized void runClient() {
		runningThread = Thread.currentThread();
		try {
			int port;
			Logger logger = RadiomanPlugin.inst.getLogger();
			
			try (Scanner scanner = new Scanner(new File(RadiomanConstants.DATA_FOLDER, "config.cfg"))) {
				port = scanner.nextInt();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				RadiomanPlugin.shutdown();
				return;
			}
			
			setCallback(RadiomanClient::handleRequest);
			
			int failedAttempts = 0;
			
			while (KEEP_RUNNING.get()) {
				if (failedAttempts < 3) logger.info("Attempting connection to Telegram relay at localhost:" + port + "...");
				try (Socket socket = new Socket(InetAddress.getLocalHost(), port)) {
					failedAttempts = 0;
					logger.info("Connected to relay");
					
					open(
							new DataInputStream(socket.getInputStream()),
							new DataOutputStream(socket.getOutputStream())
						);
					
					while (KEEP_RUNNING.get()) {
						processRequest();
					}
					
				} catch (IOException e) {
					failedAttempts++;
					close();
					if (failedAttempts < 3) logger.warning("Disconnected from relay");
				} finally {
					try {
						Thread.sleep(10_000);
					} catch (InterruptedException ignore) {}
				}
			}
		} finally {
			runningThread = null;
		}
	}

	public static void stop() {
		KEEP_RUNNING.set(false);
		if (runningThread != null) {
			runningThread.interrupt();
		}
	}
	
	public static void handleRequest(int type, DataInput input) throws IOException {
		switch (type) {
		case TYPE_TELEGRAM_BROADCAST:
			Bukkit.broadcastMessage(input.readUTF());
			break;
		}
	}

}
