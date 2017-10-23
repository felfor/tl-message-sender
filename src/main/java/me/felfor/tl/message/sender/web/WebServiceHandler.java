package me.felfor.tl.message.sender.web;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.TimeoutHandler;
import me.felfor.tl.message.sender.MessageSender;

/**
 * @author felfor
 * @since 10/16/17
 */
class WebServiceHandler {
	private static final String VERSION = "/v1";
	private static final String ADD_CLIENT_PATH = VERSION + "/addClient";
	private static final String CHANGE_CLIENT_PATH = VERSION + "/changeClient";
	private static final String SEND_MESSAGE_PATH = VERSION + "/sendMessage";
	private static final String SEND_CONFIRMATION_CODE_PATH = VERSION + "/sendConfirmationCode";
	private static final String PHONE_NUMBER = "phone_number";

	Router addHandlers(Vertx vertx) {
		Router router = Router.router(vertx);
		//GET Handlers
		router.get(ADD_CLIENT_PATH).handler(this::handleAddClient).failureHandler(ctx -> ctx.fail(ctx.failure()));
		router.get(CHANGE_CLIENT_PATH).handler(this::handleChangeClient).failureHandler(ctx -> ctx.fail(ctx.failure()));
		router.get(SEND_MESSAGE_PATH).handler(this::handleSendMessage).failureHandler(ctx -> ctx.fail(ctx.failure()));
		router.get(SEND_CONFIRMATION_CODE_PATH).handler(this::handleSendConfirmationCode)
				.failureHandler(ctx -> ctx.fail(ctx.failure()));

		//POST Handlers
		router.get(ADD_CLIENT_PATH).handler(this::handleAddClient).failureHandler(ctx -> ctx.fail(ctx.failure()));
		router.get(CHANGE_CLIENT_PATH).handler(this::handleChangeClient).failureHandler(ctx -> ctx.fail(ctx.failure()));
		router.get(SEND_MESSAGE_PATH).handler(this::handleSendMessage).failureHandler(ctx -> ctx.fail(ctx.failure()));
		router.get(SEND_CONFIRMATION_CODE_PATH).handler(this::handleSendConfirmationCode)
				.failureHandler(ctx -> ctx.fail(ctx.failure()));
		return router;
	}

	private void handleSendMessage(RoutingContext routingContext) {
		String phoneNumber = routingContext.request().getParam(PHONE_NUMBER);
		String message = routingContext.request().getParam("message");
		String firstName = routingContext.request().getParam("first_name");
		String lastName = routingContext.request().getParam("last_name");
		if (phoneNumber == null || message == null)
			throw new RuntimeException(
					"phone_number and message cannot be empty. optional params:first_name and last_name");
		MessageSender.getInstance().sendMessage(phoneNumber, firstName, lastName, message);
		writeResponse(routingContext.response(), "success");
	}

	private void handleSendConfirmationCode(RoutingContext routingContext) {
		String phoneNumber = routingContext.request().getParam(PHONE_NUMBER);
		String code = routingContext.request().getParam("code");
		if (phoneNumber == null || code == null)
			throw new RuntimeException("phone_number and code cannot be empty.");
		MessageSender.getInstance().sendConfirmationCode(phoneNumber, code);
		writeResponse(routingContext.response(), "successfully registered");
	}

	private void handleChangeClient(RoutingContext routingContext) {
		String phoneNumber = routingContext.request().getParam(PHONE_NUMBER);
		MessageSender.getInstance().changeClient(phoneNumber);
		writeResponse(routingContext.response(), "client has been changed successfully");
	}

	private final JsonObject jsonDefaultMessage = new JsonObject().put("status", "200");

	private void writeResponse(HttpServerResponse response, String message) {
		response.putHeader("content-type", "application/json")
				.end(jsonDefaultMessage.copy().put("message", message).encodePrettily());
	}

	private void handleAddClient(RoutingContext routingContext) {
		String phoneNumber = routingContext.request().getParam(PHONE_NUMBER);
		String overwrite = routingContext.request().getParam("overwrite");
		MessageSender.getInstance().addClient(phoneNumber, overwrite != null && "true".equals(overwrite.toLowerCase()));
		writeResponse(routingContext.response(), "success");
	}

}
