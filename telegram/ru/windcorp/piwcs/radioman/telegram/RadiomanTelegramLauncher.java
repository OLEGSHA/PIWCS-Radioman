package ru.windcorp.piwcs.radioman.telegram;

import ru.windcorp.piwcs.radioman.RadiomanTemplates;

import static ru.windcorp.piwcs.radioman.telegram.ServerStatusDisplayer.registerOutputField;

public class RadiomanTelegramLauncher {

	public static void main(String[] args) {
		try {
			System.out.println("Starting PIWCS Radioman Telegram Relay");
			
			System.out.print("Loading templates...");
			RadiomanTemplates.load();
			System.out.println(" done");
			
			Config.load();
			
			System.out.print("Loading server status display...");
			ServerStatusDisplayer.load();
			addDisplayFields();
			System.out.println(" done");
			
			System.out.print("Registering shutdown hook... ");
			Runtime.getRuntime().addShutdownHook(new Thread(RadiomanBot::stop, "Shutdown Hook"));
			System.out.println(" done");
			
			RadiomanBot.init();
			
			System.out.println("Startup complete\n");
			
		} catch (Exception e) {
			ServerStatusDisplayer.setTerminationCause("exception_in_init");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void addDisplayFields() {
		registerOutputField("<TERMINATION_CAUSE>", ServerStatusDisplayer::getTerminationCause);
		registerOutputField("<SERVER_ONLINE>", RadiomanEngineTelegram::isOnline);
		registerOutputField("<SERVER_PLAYERS>", RadiomanEngineTelegram.getPlayerlist()::getSize);
		registerOutputField("<TIME_MS>", System::currentTimeMillis);
	}

}
