package me.felfor.tl.message.sender.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.felfor.tl.message.sender.MessageSender;
import me.felfor.tl.message.sender.objects.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.bot.kernel.database.DatabaseManager;
import org.telegram.bot.structure.Chat;
import org.telegram.bot.structure.IUser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author felfor
 * @since 10/15/17
 */
public class FileDatabaseManagerImpl implements DatabaseManager {
	//	private static final String CHATS_FILE_NAME = "db_chats.json";
	//	private static final String USERS_FILES_NAME = "db_users.json";
	private static final String DIFFERENCES_FILE_NAME = "db_differences.json";
	private static final String TARGET_USERS_FILES_NAME = "db_target_users.json";

	//	private final List<Map<String, Object>> chats;
	//	private final List<Map<String, Object>> users;
	private final Map<String, List<Map<String, Object>>> differences;
	private final Map<String, User> targetUsers;
	private static final int MAXIMUM_TARGETS_BUFFER_SIZE = 10000;

	public FileDatabaseManagerImpl() {
		try {
			//			this.chats = loadJson(CHATS_FILE_NAME);
			//			this.users = loadJson(USERS_FILES_NAME);
			this.differences = loadDifferences();
			this.targetUsers = loadTargetUsers(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, List<Map<String, Object>>> loadDifferences() throws IOException {
		List<Map<String, Object>> maps = loadJson(DIFFERENCES_FILE_NAME);
		Map<String, List<Map<String, Object>>> differences = null;
		if (maps != null)
			differences = maps.stream().collect(Collectors.groupingBy(i -> i.get("client").toString()));
		if (differences == null)
			differences = new HashMap<>();
		return differences;
	}

	private static final Type USER_TYPE = new TypeToken<List<User>>() {
	}.getType();

	private Map<String, User> loadTargetUsers(boolean limitResults) throws IOException {
		Gson gson = new Gson();
		File file = new File(TARGET_USERS_FILES_NAME);
		if (file.exists()) {
			JsonReader reader = new JsonReader(new FileReader(TARGET_USERS_FILES_NAME));
			List<User> data = gson.fromJson(reader, USER_TYPE);
			if (data != null) {
				if (limitResults && data.size() > MAXIMUM_TARGETS_BUFFER_SIZE)
					data = data.subList(data.size() - MAXIMUM_TARGETS_BUFFER_SIZE, data.size());
				return data.stream().collect(Collectors.toMap(User::getPhoneNumber, Function.identity()));
			}
		} else
			file.createNewFile();
		return new HashMap<>();
	}

	private List<Map<String, Object>> loadJson(String fileName) throws IOException {
		File file = new File(fileName);
		if (file.exists()) {
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new FileReader(fileName));
			return gson.fromJson(reader, List.class);
		} else
			file.createNewFile();

		return null;
	}

	@Override public @Nullable Chat getChatById(int chatId) {
		//		Optional<Map<String, Object>> optionalChat = chats.stream().filter(i -> i.get("id").equals(chatId)).findFirst();
		return null;
	}

	@Override public @Nullable IUser getUserById(int userId) {
		return null;
	}

	@Override public @NotNull Map<Integer, int[]> getDifferencesData() {
		Map<Integer, int[]> result = new HashMap<>();
		if (differences.containsKey(MessageSender.getInstance().getCurrentClientNumber()))
			for (Map<String, Object> difference : differences
					.get(MessageSender.getInstance().getCurrentClientNumber())) {
				final int[] differencesData = new int[3];
				differencesData[0] = (int) difference.get("pts");
				differencesData[1] = (int) difference.get("date");
				differencesData[2] = (int) difference.get("seq");
				result.put((Integer) difference.get("botId"), differencesData);
			}
		return result;
	}

	@Override synchronized public boolean updateDifferencesData(int botId, int pts, int date, int seq) {
		Map<String, Object> record = new HashMap<>();
		record.put("pts", pts);
		record.put("date", date);
		record.put("seq", seq);
		record.put("botId", botId);
		record.put("client", MessageSender.getInstance().getCurrentClientNumber());
		List<Map<String, Object>> maps = differences
				.computeIfAbsent(MessageSender.getInstance().getCurrentClientNumber(), k -> new ArrayList<>());
		//because in this case we don't need to handle replacement
		maps.clear();
		maps.add(record);
		try (FileWriter fw = new FileWriter(DIFFERENCES_FILE_NAME)) {
			new Gson().toJson(differences.values().stream().flatMap(Collection::stream).collect(Collectors.toList()),
					List.class, new JsonWriter(fw));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//useless return variable
		return false;
	}

	public boolean isRegistered(String phoneNumber) {
		try {
			return StreamSupport
					.stream(Files.newDirectoryStream(Paths.get("."), path -> path.toString().endsWith(".auth"))
							.spliterator(), false)
					.anyMatch(file -> file.getFileName().toString().contains(phoneNumber));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void addUser(User user) {
		try (FileWriter fw = new FileWriter(TARGET_USERS_FILES_NAME)) {
			Map<String, User> allTargets = loadTargetUsers(false);
			allTargets.put(user.getPhoneNumber(), user);
			new Gson().toJson(allTargets.values(), List.class, new JsonWriter(fw));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public User getTargetUser(String phoneNumber) {
		User user = targetUsers.get(phoneNumber);
		if (user == null && targetUsers.size() >= MAXIMUM_TARGETS_BUFFER_SIZE)
			try {
				user = loadTargetUsers(false).get(phoneNumber);
			} catch (IOException e) {
				e.printStackTrace();
			}
		return user;
	}

	public static void main(String[] args) {
		new FileDatabaseManagerImpl().addUser(new User(1, 2L, "s"));
	}

	public void removeClient(String phoneNumber) {
		try {
			Optional<Path> first = StreamSupport
					.stream(Files.newDirectoryStream(Paths.get("."), path -> path.toString().endsWith(".auth"))
							.spliterator(), false).filter(file -> file.getFileName().toString().contains(phoneNumber))
					.findFirst();
			if (first.isPresent())
				Files.delete(first.get());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
