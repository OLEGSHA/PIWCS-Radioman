package ru.windcorp.piwcs.radioman.telegram;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import ru.windcorp.piwcs.radioman.RadiomanConstants;

public class ServerStatusDisplayer {
	
	private static String template;
	
	private static ScheduledExecutorService scheduler =
			Executors.newSingleThreadScheduledExecutor(r -> {
				Thread thread = new Thread(r, "Displayer");
				thread.setDaemon(true);
				return thread;
			});
	
	private static long regularUpdateDelayInSeconds;
	private static ScheduledFuture<?> regularUpdate = null;
	
	private static final Collection<OutputField> OUTPUT_FIELDS = new ArrayList<>();
	
	private static String terminationCause = null;
	
	public static void load() throws IOException {
		StringBuilder sb = new StringBuilder();
		
		System.out.print("reading display template...");
		try (Reader reader = Files.newBufferedReader(Paths.get(RadiomanConstants.DATA_FOLDER.getPath(), "display_template.txt"))) {
			while (true) {
				int c = reader.read();
				if (c < 0) {
					break;
				} else {
					sb.append((char) c);
				}
			}
		}
		
		template = sb.toString();
		regularUpdateDelayInSeconds = (long) (Config.getDisplayUpdateFrequency() * 60);
		
		update();
		scheduleRegularUpdate();
	}
	
	private static class OutputField {
		private final String marker;
		private final Supplier<Object> supplier;
		
		public OutputField(String marker, Supplier<Object> supplier) {
			this.marker = marker;
			this.supplier = supplier;
		}
		
		public void insert(StringBuilder buffer) {
			String data = String.valueOf(this.supplier.get());
			
			int index = 0;
			while ((index = buffer.indexOf(marker, index)) >= 0) {
				buffer.replace(index, index + marker.length(), data);
			}
		}
	}
	
	public static void regularUpdate() {
		regularUpdate = null;
		update();
	}
	
	public static void update() {
		if (regularUpdate != null) {
			regularUpdate.cancel(false);
		}
		
		StringBuilder buffer = new StringBuilder(template);
		OUTPUT_FIELDS.forEach(r -> r.insert(buffer));
		
		try {
			Files.write(Config.getDisplayFile(), buffer.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void scheduleRegularUpdate() {
		regularUpdate = scheduler.schedule(ServerStatusDisplayer::regularUpdate, regularUpdateDelayInSeconds, TimeUnit.SECONDS);
	}
	
	public static void registerOutputField(String marker, Supplier<Object> supplier) {
		OUTPUT_FIELDS.add(new OutputField(marker, supplier));
	}

	public static String getTerminationCause() {
		return terminationCause;
	}

	public static void setTerminationCause(String terminationCause) {
		ServerStatusDisplayer.terminationCause = terminationCause;
	}

}
