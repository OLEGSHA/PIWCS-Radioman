package ru.windcorp.piwcs.radioman.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ru.windcorp.piwcs.radioman.RadiomanConstants;
import ru.windcorp.piwcs.radioman.RadiomanIntercoms;

import static ru.windcorp.piwcs.radioman.RadiomanTemplates.tpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class RadiomanEngineTelegram {
	
	private static final String NAME_PENDING = "NOT-VERIFIED---------";
	
	private static final Map<Integer, String> DATABASE = new HashMap<>();
	
	private static final Playerlist PLAYERLIST = new Playerlist();
	
	private static boolean isOnline = false;
	
	public static Map<Integer, String> getDatabase() {
		return DATABASE;
	}
	
	public static Playerlist getPlayerlist() {
		return PLAYERLIST;
	}

	public static String getVerifiedName(int userId) {
		String name = DATABASE.get(userId);
		if (name == null || name.equals(NAME_PENDING)) {
			return null;
		}
		return name;
	}
	
	public static boolean isNamePendingVerification(int userId) {
		return NAME_PENDING.equals(DATABASE.get(userId));
	}
	
	public static boolean hasUserAppeared(int userId) {
		return DATABASE.containsKey(userId);
	}

	public static void loadDatabase() throws IOException, NoSuchElementException {
		System.out.print("Loading database... ");
		try (Scanner scanner = new Scanner(new File(RadiomanConstants.DATA_FOLDER, "database.db"))) {
			System.out.print("file opened; ");
			while (scanner.hasNext()) {
				DATABASE.put(scanner.nextInt(), scanner.next());
				System.out.print('.');
			}
		}
		System.out.println(" done");
	}

	public static void saveDatabase() throws FileNotFoundException {
		System.out.print("Saving database... ");
		try (PrintStream output = new PrintStream(new File(RadiomanConstants.DATA_FOLDER, "database.db"))) {
			System.out.print("file opened; ");
			DATABASE.forEach((userId, name) -> {
				output.print(userId);
				output.print(" ");
				output.println(name);
				System.out.print('.');
			});
		}
		System.out.println(" done");
	}
	
	public static void onOnline() {
		System.out.println("Client connected");
		send(new SendMessage(RadiomanBot.getMainChatId(), tpl("online")));
		isOnline = true;
		ServerStatusDisplayer.update();
	}
	
	public static void onOffline() {
		System.out.println("Client disconnected");
		send(new SendMessage(RadiomanBot.getMainChatId(), tpl("offline")));
		isOnline = false;
		ServerStatusDisplayer.update();
	}
	
	public static boolean isOnline() {
		return isOnline;
	}

	public static void processMessage(Message message, boolean isEdited) {
		if (message.getChatId() != RadiomanBot.getMainChatId()) {
			System.out.print("Not main chat, complaining. ");
			replyTo(message, tpl("telegram_use_group_chat"));
			return;
		}

		if (!RadiomanIntercoms.isOpen()) {
			System.out.print("Not connected, quietly ignoring. ");
			return;
		}
		
		getPlayerlist().invalidate();
		
		if (message.isCommand()) {
			System.out.print("Command. ");
			processCommand(message);
			return;
		}
		
		if (message.getNewChatTitle() != null ||
				message.getNewChatPhoto() != null ||
				message.getDeleteChatPhoto() != null ||
				message.getGroupchatCreated() != null ||
				message.getSuperGroupCreated() != null ||
				message.getChannelChatCreated() != null ||
				message.getMigrateToChatId() != null ||
				message.getMigrateFromChatId() != null ||
				message.getInvoice() != null ||
				message.getSuccessfulPayment() != null ||
				message.getPassportData() != null) {
			System.out.print("Off-topic, quietly ignoring. ");
			// Ignore
		} else if (message.getNewChatMembers() != null) {
			System.out.print("New members. ");
			processNewMembers(message);
		} else if (message.getLeftChatMember() != null) {
			System.out.print("Left members. ");
			processLeftMember(message);
		} else if (isEdited || message.getText() != null) {
			System.out.print("Text. ");
			processPostedMessage(message, isEdited);
		} else {
			System.out.print("Unknown, complaining. ");
			replyTo(message, tpl("telegram_cannot_handle"));
		}
	}

	private static void processCommand(Message message) {
		// No commands for now
	}

	private static void processPostedMessage(Message message, boolean isEdited) {
		String name = getNameFor(message.getFrom());
		String text = RadiomanTelegramFormatter.translateFormatting(message);
		
		if (isEdited) {
			text += "\u00a7e *";
		}
		
		if (message.getForwardDate() != null) {
			RadiomanRelay.broadcast(tpl(
					"telegram_text_forward",
					name, text));
			return;
		} else if (message.getReplyToMessage() != null) {
			Message replyTo = message.getReplyToMessage();
			String replyToText = replyTo.getText();
			
			if (replyToText.length() > 20) {
				replyToText = replyToText.substring(0, 20).trim() + " [...]";
			}
			
			RadiomanRelay.broadcast(tpl(
					"telegram_text_reply",
					name, text,
					getNameFor(replyTo.getFrom()),
					replyToText));
			return;
		}
		
		RadiomanRelay.broadcast(tpl(
				"telegram_text",
				name, text));
	}

	private static String getNameFor(User from) {
		String name = getVerifiedName(from.getId());
		if (name == null) {
			name = "\u00a71[T]\u00a79" + from.getFirstName();
		}
		return name;
	}

	private static void processNewMembers(Message message) {
		for (User user : message.getNewChatMembers()) {
			RadiomanRelay.broadcast(tpl("telegram_new_user", getNameFor(user)));
		}
	}

	private static void processLeftMember(Message message) {
		RadiomanRelay.broadcast(tpl("telegram_left_user", getNameFor(message.getLeftChatMember())));
	}
	
	public static void replyTo(Message message, String text) {
		SendMessage send = new SendMessage(message.getChatId(), text);
		send.setReplyToMessageId(message.getMessageId());
		send(send);
	}
	
	public static Message send(String text) {
		return send(new SendMessage(RadiomanBot.getMainChatId(), text));
	}
	
	public static Message send(SendMessage message) {
		try {
			message.enableMarkdown(true);
			return RadiomanBot.getInst().execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Message edit(Message message, String text) {
		try {
			EditMessageText action = new EditMessageText();
			action.setChatId(message.getChatId());
			action.setMessageId(message.getMessageId());
			action.setText(text);
			action.enableMarkdown(true);
			
			Object obj = RadiomanBot.getInst().execute(action);
			if (obj instanceof Message) {
				return (Message) obj;
			} else {
				return null;
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
			return null;
		}
	}

}
