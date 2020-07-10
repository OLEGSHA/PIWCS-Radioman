package ru.windcorp.piwcs.radioman.telegram;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.DefaultBotOptions.ProxyType;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.BotSession;

public class RadiomanBot extends TelegramLongPollingBot {
	
	private static RadiomanBot inst = null; 
	private static BotSession session = null;

	public RadiomanBot(DefaultBotOptions options) {
		super(options);
	}

	@Override
	public String getBotUsername() {
		return Config.getUsername();
	}

	@Override
	public String getBotToken() {
		return Config.getToken();
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage()) {
			System.out.print("Message (new).  ");
			RadiomanEngineTelegram.processMessage(update.getMessage(), false);
			System.out.println("Done.");
			return;
		}
		
		if (update.hasEditedMessage()) {
			System.out.print("Message (edit). ");
			RadiomanEngineTelegram.processMessage(update.getEditedMessage(), true);
			System.out.println("Done.");
			return;
		}
		
		System.out.println("Update without message, ignoring");
	}
	
	public static void init() throws Exception {
		RadiomanEngineTelegram.loadDatabase();
		registerBot();
		RadiomanRelay.runServer(Config.getPort());
	}

	private static void registerBot() throws Exception {
		System.out.print("Registering bot... ");
		ApiContextInitializer.init();
		TelegramBotsApi api = new TelegramBotsApi();
		
		DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
		
		setupProxy(botOptions);
		
		inst = new RadiomanBot(botOptions);
		session = api.registerBot(inst);
		
		System.out.println("Done");
	}
	
	private static void setupProxy(DefaultBotOptions botOptions) throws Exception {
		if (Config.getProxyType() != ProxyType.NO_PROXY) {
			botOptions.setProxyHost(Config.getProxyHost());
			botOptions.setProxyPort(Config.getProxyPort());
			botOptions.setProxyType(Config.getProxyType());
			
			setupAuthenticator();
		}
	}

	private static void setupAuthenticator() throws Exception {
		Authenticator.setDefault(new Authenticator() {
			
			@Override
			protected synchronized PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Config.getProxyUsername(), Config.getProxyPassword());
			}
			
		});
	}
	
	public static void stop() {
		System.out.println("Stopping");
		RadiomanRelay.stop();
		if (session != null) session.stop();
		
		if (ServerStatusDisplayer.getTerminationCause() == null) ServerStatusDisplayer.setTerminationCause("manual");
		ServerStatusDisplayer.update();
	}
	
	public static RadiomanBot getInst() {
		return inst;
	}

	public static long getMainChatId() {
		return Config.getMainChatId();
	}

}
