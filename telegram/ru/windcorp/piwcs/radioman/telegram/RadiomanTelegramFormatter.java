package ru.windcorp.piwcs.radioman.telegram;

import java.util.Arrays;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

public class RadiomanTelegramFormatter {
	
	private static final int BOLD = 1, ITALICS = 2, URL = 4;
	private static final short[] INDEX_BUFFER = new short[4096];
	private static final byte[] DATA_BUFFER = new byte[4096];
	
	public static synchronized String translateFormatting(Message message) {
		List<MessageEntity> entities = message.getEntities();
		if (entities == null) {
			return message.getText();
		}
		
		try {
			StringBuilder text = new StringBuilder(message.getText());
			int length = text.length();
			Arrays.fill(DATA_BUFFER, 0, length, (byte) 0);
			
			int indexIndex = 0;
			for (MessageEntity entity : entities) {
				int start = entity.getOffset();
				int end = entity.getOffset() + entity.getLength();
				
				switch (entity.getType()) {
				case "bold":
					if (DATA_BUFFER[start] == 0) INDEX_BUFFER[indexIndex++] = (short) (int) start;
					if (DATA_BUFFER[  end] == 0) INDEX_BUFFER[indexIndex++] = (short) (int) end;
					DATA_BUFFER[start] |= BOLD;
					DATA_BUFFER[  end] |= BOLD;
					break;
				case "italic":
					if (DATA_BUFFER[start] == 0) INDEX_BUFFER[indexIndex++] = (short) (int) start;
					if (DATA_BUFFER[  end] == 0) INDEX_BUFFER[indexIndex++] = (short) (int) end;
					DATA_BUFFER[start] |= ITALICS;
					DATA_BUFFER[  end] |= ITALICS;
					break;
				case "url":
					if (DATA_BUFFER[start] == 0) INDEX_BUFFER[indexIndex++] = (short) (int) start;
					if (DATA_BUFFER[  end] == 0) INDEX_BUFFER[indexIndex++] = (short) (int) end;
					DATA_BUFFER[start] |= URL;
					DATA_BUFFER[  end] |= URL;
					break;
				}
			}
			
			Arrays.sort(INDEX_BUFFER, 0, indexIndex);
			
			boolean isBold = false, isItalics = false, isURL = false;
			boolean recoverFormatting = false;
			
			int di = 0;
			for (int i = 0; i < indexIndex; ++i) {
				int index = INDEX_BUFFER[i];
				
				if ((DATA_BUFFER[index] & URL) != 0) {
					if (!isURL) {
						text.insert(index + di, "\u00a7f");
						di += 2;
					}
					recoverFormatting = true;
					
					isURL = !isURL;
				}
				if ((DATA_BUFFER[index] & BOLD) != 0) {
					if (isBold) {
						text.insert(index + di, "\u00a7r");
						di += 2;
						recoverFormatting = true;
					} else {
						text.insert(index + di, "\u00a7l");
						di += 2;
					}
					
					isBold = !isBold;
				}
				if ((DATA_BUFFER[index] & ITALICS) != 0) {
					if (isItalics) {
						text.insert(index + di, "\u00a7r");
						di += 2;
						recoverFormatting = true;
					} else {
						text.insert(index + di, "\u00a7o");
						di += 2;
					}
					
					isItalics = !isItalics;
				}
				
				if (recoverFormatting) {
					recoverFormatting = false;

					if (isURL) {
						text.insert(index + di, "\u00a79\u00a7n");
						di += 4;
					}
					if (isBold) {
						text.insert(index + di, "\u00a7l");
						di += 2;
					}
					if (isItalics) {
						text.insert(index + di, "\u00a7o");
						di += 2;
					}
				}
			}
			
			return text.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return message.getText();
		}
	}

}
