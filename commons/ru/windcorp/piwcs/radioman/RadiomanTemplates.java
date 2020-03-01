package ru.windcorp.piwcs.radioman;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class RadiomanTemplates {
	
	private static final Map<String, String> MAP = new HashMap<>();
	
	public static void load() throws FileNotFoundException {
		try (Scanner scanner = new Scanner(new File(RadiomanConstants.DATA_FOLDER, "templates.cfg"))) {
			while (scanner.hasNext()) {
				MAP.put(scanner.next(), translateAlternateColorCodes('&', scanner.nextLine().trim().replace('^', '\n')));
			}
		}
	}
	
	private static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
		char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = '\u00a7';
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
	}

	public static String tpl(String key, Object... args) {
		return String.format(MAP.getOrDefault(key, key), args);
	}

}
