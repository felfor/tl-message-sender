package me.felfor.tl.message.sender;

import me.felfor.tl.message.sender.impl.BotConfigImpl;
import me.felfor.tl.message.sender.impl.ChatUpdatesBuilderImpl;
import me.felfor.tl.message.sender.impl.FileDatabaseManagerImpl;
import me.felfor.tl.message.sender.objects.User;
import me.felfor.tl.message.sender.utils.PhoneNumberNormalizer;
import org.telegram.bot.kernel.TelegramBot;
import org.telegram.bot.structure.LoginStatus;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author felfor
 * @since 10/15/17
 */
public class MessageSender {
	private static String apiHash;
	private static int apiKey;
	private final FileDatabaseManagerImpl databaseManager;
	private String currentClientNumber;
	private ReentrantReadWriteLock lock;
	private ReentrantLock clientRegistrationLock;
	private TelegramBot currentBot;
	private String inRegistrationPhoneNumber;
	private TelegramBot inRegistrationTelegramBot;
	private static MessageSender instance = new MessageSender();

	private MessageSender() {
		this.databaseManager = new FileDatabaseManagerImpl();
		this.lock = new ReentrantReadWriteLock();
		this.clientRegistrationLock = new ReentrantLock();
	}

	public void addClient(String phoneNumber, boolean overwrite) {
		phoneNumber = PhoneNumberNormalizer.normalize(phoneNumber);
		if (!overwrite && alreadyRegistered(phoneNumber))
			throw new RuntimeException(
					MessageFormat.format("The Telegram account with number {0} already registered!", phoneNumber));
		try {
			databaseManager.removeClient(phoneNumber);
			clientRegistrationLock.tryLock(5, TimeUnit.MINUTES);
			registerClient(phoneNumber);
		} catch (InterruptedException e) {
			clientRegistrationLock.unlock();
			throw new RuntimeException(MessageFormat
					.format("another phone registration for number: {0} is waiting for your input",
							inRegistrationPhoneNumber));
		} catch (Exception registrationException) {
			clientRegistrationLock.unlock();
			throw new RuntimeException(registrationException);
		}
	}

	public void sendConfirmationCode(String phoneNumber, String code) {
		phoneNumber = PhoneNumberNormalizer.normalize(phoneNumber);
		if (phoneNumber.equals(inRegistrationPhoneNumber)) {
			boolean success = inRegistrationTelegramBot.getKernelAuth().setAuthCode(code);
			if (!success)
				throw new RuntimeException("confirmation failed. maybe this code expired or code is mismatch");

		} else
			throw new RuntimeException(
					"mismatch phone number. first call method addClient(phoneNumber,overwrite) and then use this method");
	}

	public void sendMessage(String targetPhoneNumber, String firstName, String lastName, String message) {
		targetPhoneNumber = PhoneNumberNormalizer.normalize(targetPhoneNumber);
		try {
			lock.readLock().lock();
			User targetUser = databaseManager.getTargetUser(targetPhoneNumber);
			if (targetUser == null) {
				try {
					User userAccount = TelegramAPI
							.getUserAccount(targetPhoneNumber, firstName, lastName, apiKey, currentBot);
					databaseManager.addUser(userAccount);
					TelegramAPI.sendMessage(userAccount, message, currentBot);
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	public void changeClient(String phoneNumber) {
		phoneNumber = PhoneNumberNormalizer.normalize(phoneNumber);
		if (databaseManager.isRegistered(phoneNumber)) {
			try {
				lock.writeLock().lock();
				BotConfigImpl botConfig = new BotConfigImpl(phoneNumber);
				botConfig.setHashCode(apiHash);
				TelegramBot candidateTelegramBot = new TelegramBot(botConfig,
						new ChatUpdatesBuilderImpl(databaseManager), apiKey, apiHash);
				if (candidateTelegramBot.init() == LoginStatus.ALREADYLOGGED) {
					currentBot = candidateTelegramBot;
					currentClientNumber = phoneNumber;
					return;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				lock.writeLock().unlock();
			}
			throw new RuntimeException(MessageFormat.format("cannot change to client {0}", phoneNumber));
		}
	}

	private void registerClient(String phoneNumber) throws Exception {
		inRegistrationPhoneNumber = phoneNumber;
		inRegistrationTelegramBot = new TelegramBot(new BotConfigImpl(phoneNumber),
				new ChatUpdatesBuilderImpl(databaseManager), apiKey, apiHash);
		LoginStatus loginStatus = inRegistrationTelegramBot.init();
		if (loginStatus != LoginStatus.CODESENT)
			throw new RuntimeException(MessageFormat.format("an unexpected status: {0} happened", loginStatus));
	}

	private boolean alreadyRegistered(String phoneNumber) {
		return databaseManager.isRegistered(phoneNumber);
	}

	public String getCurrentClientNumber() {
		return currentClientNumber;
	}

	public static MessageSender getInstance() {
		return instance;
	}

	static void setApiHash(String apiHash) {
		MessageSender.apiHash = apiHash;
	}

	static void setApiKey(int apiKey) {
		MessageSender.apiKey = apiKey;
	}
}
