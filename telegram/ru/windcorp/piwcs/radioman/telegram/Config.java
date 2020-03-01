package ru.windcorp.piwcs.radioman.telegram;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import ru.windcorp.piwcs.radioman.RadiomanConstants;

public class Config {
	
	private static String username, token;
	private static String proxyHost, proxyUsername;
	private static int proxyPort;
	private static char[] proxyPassword;
	
	private static int port;
	
	private static long mainChatId;
	
	private static Path displayFile;
	private static float displayUpdateFrequency;

	public static void load() throws IOException {
		System.out.println("Loading config... ");
		try (Scanner scanner = new Scanner(new File(RadiomanConstants.DATA_FOLDER, "config.cfg"))) {
			System.out.println("\tfile opened");
			port = scanner.nextInt();                       System.out.println("\tport = " + port);
			username = scanner.next();                      System.out.println("\tusername = " + username);
			token = scanner.next();                         System.out.println("\ttoken = [REDACTED]");
			proxyHost = scanner.next();                     System.out.println("\tproxyHost = " + proxyHost);
			proxyPort = scanner.nextInt();                  System.out.println("\tproxyPort = " + proxyPort);
			proxyUsername = scanner.next();                 System.out.println("\tproxyUsername = " + proxyUsername);
			proxyPassword = scanner.next().toCharArray();   System.out.println("\tproxyPassword = [REDACTED]");
			mainChatId = scanner.nextLong();                System.out.println("\tmainChatId = " + mainChatId);
			displayFile = Paths.get(scanner.next());        System.out.println("\tdisplayFile = " + displayFile);
			displayUpdateFrequency = scanner.nextFloat();   System.out.println("\tdisplayUpdateFrequency = " + displayFile);
		}
		System.out.println("done");
	}

	public static String getUsername() {
		return username;
	}

	public static String getToken() {
		return token;
	}

	public static String getProxyHost() {
		return proxyHost;
	}

	public static String getProxyUsername() {
		return proxyUsername;
	}

	public static int getProxyPort() {
		return proxyPort;
	}

	public static char[] getProxyPassword() {
		return proxyPassword;
	}

	public static int getPort() {
		return port;
	}

	public static long getMainChatId() {
		return mainChatId;
	}
	
	public static Path getDisplayFile() {
		return displayFile;
	}

	public static float getDisplayUpdateFrequency() {
		return displayUpdateFrequency;
	}
}
