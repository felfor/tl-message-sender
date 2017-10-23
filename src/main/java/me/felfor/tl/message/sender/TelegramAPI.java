package me.felfor.tl.message.sender;

import me.felfor.tl.message.sender.objects.User;
import org.telegram.api.contacts.TLAbsContacts;
import org.telegram.api.contacts.TLContacts;
import org.telegram.api.contacts.TLImportedContacts;
import org.telegram.api.engine.RpcException;
import org.telegram.api.functions.contacts.TLRequestContactsGetContacts;
import org.telegram.api.functions.contacts.TLRequestContactsImportContacts;
import org.telegram.api.input.TLInputPhoneContact;
import org.telegram.api.user.TLAbsUser;
import org.telegram.api.user.TLUser;
import org.telegram.bot.kernel.TelegramBot;
import org.telegram.tl.TLVector;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author felfor
 * @since 10/15/17
 */
class TelegramAPI {

	static User getUserAccount(String phoneNumber, String firstName, String lastName, long clientId, TelegramBot kernel)
			throws ExecutionException, RpcException {
		User currentContactUser = getUserFromContacts(phoneNumber, kernel);
		if (currentContactUser != null)
			return currentContactUser;
		TLRequestContactsImportContacts req = new TLRequestContactsImportContacts();
		TLInputPhoneContact contact = new TLInputPhoneContact();
		contact.setPhone(phoneNumber);
		contact.setFirstName(firstName == null ? "u" + phoneNumber : firstName);
		contact.setLastName(lastName == null ? "" : lastName);
		contact.setClientId(clientId);
		TLVector<TLInputPhoneContact> contacts = new TLVector<>();
		contacts.add(contact);
		req.setContacts(contacts);
		TLImportedContacts result = kernel.getKernelComm().doRpcCallSync(req);
		if (result.getUsers() == null || result.getUsers().isEmpty())
			throw new RuntimeException(
					MessageFormat.format("There is no Telegram account with number {1} ", phoneNumber));
		return new User(result.getUsers().get(0).getId(), ((TLUser) result.getUsers().get(0)).getAccessHash(),
				phoneNumber);
	}

	private static User getUserFromContacts(String phoneNumber, TelegramBot kernel)
			throws ExecutionException, RpcException {
		TLRequestContactsGetContacts req = new TLRequestContactsGetContacts();
		req.setHash(kernel.getConfig().getHashCode());
		TLAbsContacts tlAbsContacts = kernel.getKernelComm().doRpcCallSync(req);
		if (tlAbsContacts != null) {
			Optional<TLAbsUser> first = ((TLContacts) tlAbsContacts).getUsers().stream()
					.filter(u -> ((TLUser) u).getPhone().equals(phoneNumber.substring(1))).findFirst();
			if (first.isPresent())
				return new User((TLUser) first.get());
		}
		return null;
	}

	static void sendMessage(User user, String message, TelegramBot kernel) throws RpcException {
		kernel.getKernelComm().sendMessage(user, message);
		kernel.getKernelComm().performMarkAsRead(user, 0);
	}
}
