package me.felfor.tl.message.sender.utils;

/**
 * @author felfor
 * @since 10/16/17
 */
public class PhoneNumberNormalizer {
	public static String normalize(String phoneNumber) {
		if (phoneNumber.startsWith("0098"))
			return "+" + phoneNumber.substring(2);
		if (phoneNumber.startsWith("09"))
			return "+98" + phoneNumber.substring(1);
		if (!phoneNumber.startsWith("+"))
			return "+" + phoneNumber;
		return phoneNumber;
	}
}
