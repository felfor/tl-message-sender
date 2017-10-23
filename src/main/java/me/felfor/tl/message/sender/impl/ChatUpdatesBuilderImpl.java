package me.felfor.tl.message.sender.impl;

import org.telegram.bot.ChatUpdatesBuilder;
import org.telegram.bot.handlers.UpdatesHandlerBase;
import org.telegram.bot.kernel.IKernelComm;
import org.telegram.bot.kernel.database.DatabaseManager;
import org.telegram.bot.kernel.differenceparameters.DifferenceParametersService;
import org.telegram.bot.kernel.differenceparameters.IDifferenceParametersService;

import java.lang.reflect.InvocationTargetException;

/**
 * @author felfor
 * @since 10/15/17
 */
public class ChatUpdatesBuilderImpl implements ChatUpdatesBuilder {

	private DatabaseManager databaseManager;
	private IKernelComm kernelComm;
	private IDifferenceParametersService differenceParametersService;

	public ChatUpdatesBuilderImpl(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	@Override public UpdatesHandlerBase build()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		return new EmptyChatUpdatesHandler(kernelComm, differenceParametersService, databaseManager);
	}

	@Override public void setKernelComm(IKernelComm kernelComm) {

		this.kernelComm = kernelComm;
	}

	@Override public void setDifferenceParametersService(IDifferenceParametersService differenceParametersService) {

		this.differenceParametersService = differenceParametersService;
	}

	@Override public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}
}
