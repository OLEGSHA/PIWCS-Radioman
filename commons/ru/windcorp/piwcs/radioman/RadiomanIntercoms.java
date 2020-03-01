package ru.windcorp.piwcs.radioman;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RadiomanIntercoms {
	
	@FunctionalInterface
	public interface IntercomCallback {
		void onRequest(int type, DataInput input) throws IOException;
	}
	
	@FunctionalInterface
	public interface IntercomWriter {
		void write(DataOutput output) throws IOException;
	}

	private static DataInput input = null;
	private static DataOutput output = null;
	private static IntercomCallback callback = null;
	
	public static final int TYPE_TELEGRAM_BROADCAST = 0;
	
	public static final int TYPE_MINECRAFT_CHAT = 0;
	public static final int TYPE_MINECRAFT_JOINED = 1;
	public static final int TYPE_MINECRAFT_LEFT = 2;
	public static final int TYPE_MINECRAFT_PLAYERLIST = 3;
	
	public static synchronized void open(DataInput input, DataOutput output) {
		RadiomanIntercoms.input = input;
		RadiomanIntercoms.output = output;
	}
	
	public static synchronized void close() {
		input = null;
		output = null;
	}

	public static void processRequest() throws IOException {
		getCallback().onRequest(input.readByte(), input);
	}
	
	public static synchronized boolean isOpen() {
		return output != null;
	}
	
	public static synchronized void sendRequest(int type, IntercomWriter writer) {
		if (!isOpen()) {
			throw new IllegalStateException("No channel open");
		}
		
		try {
			output.writeByte(type);
			writer.write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static IntercomCallback getCallback() {
		return callback;
	}

	public static void setCallback(IntercomCallback callback) {
		RadiomanIntercoms.callback = callback;
	}
	
}
