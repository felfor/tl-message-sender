package me.felfor.tl.message.sender.impl;

import org.telegram.bot.structure.BotConfig;

/**
 * @author felfor
 * @since 10/15/17
 */
public class BotConfigImpl extends BotConfig {
	private String phoneNumber;

	public BotConfigImpl(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		setAuthfile(phoneNumber + ".auth");
	}

	@Override public String getPhoneNumber() {
		return phoneNumber;
	}

	@Override public String getBotToken() {
		return null;
	}

	@Override public boolean isBot() {
		return false;
	}
}
