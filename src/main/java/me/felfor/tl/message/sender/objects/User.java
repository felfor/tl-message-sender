package me.felfor.tl.message.sender.objects;

import org.telegram.api.user.TLUser;
import org.telegram.bot.structure.IUser;

/**
 * @author felfor
 * @since 10/15/17
 */
public class User implements IUser {
	private final int userId; ///< ID of the user
	private Long userHash; ///< Hash of the user
	private String phoneNumber;

	public User(User copy) {
		this.userId = copy.getUserId();
		this.userHash = copy.getUserHash();
	}

	public User(TLUser tlUser) {
		this.userHash = tlUser.getAccessHash();
		this.userId = tlUser.getId();
		this.phoneNumber = tlUser.getPhone();
	}

	public User(int userId, Long userHash, String phoneNumber) {
		this.userId = userId;
		this.userHash = userHash;
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override public int getUserId() {
		return this.userId;
	}

	@Override public Long getUserHash() {
		return userHash;
	}

	public void setUserHash(Long userHash) {
		this.userHash = userHash;
	}

	@Override public String toString() {
		return "" + this.userId;
	}
}
