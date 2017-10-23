package me.felfor.tl.message.sender;

import me.felfor.tl.message.sender.web.VertxServer;

/**
 * @author felfor
 * @since 10/17/17
 */
public class Main {
	private static int port = 8080;

	public static void main(String[] args) throws IllegalAccessException {
		try {
			parseArgs(args);
		} catch (Exception e) {
			throw new IllegalAccessException(
					"\n\n usage:\n\t -p [the port u want to start server. optional default is 8080] \n\t -h [API hash. required ]\n\t -k [API key number. required]\n");
		}
		new VertxServer().startServer(port);
	}

	private static void parseArgs(String[] args) {
		int requiredCounts = 2;
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-p":
				port = Integer.valueOf(args[++i]);
				break;
			case "-k":
				MessageSender.setApiKey(Integer.valueOf(args[++i]));
				requiredCounts--;
				break;
			case "-h":
				MessageSender.setApiHash(args[++i]);
				requiredCounts--;
				break;
			}
		}
		if (requiredCounts > 0)
			throw new RuntimeException();
	}
}