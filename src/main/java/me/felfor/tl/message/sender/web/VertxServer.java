package me.felfor.tl.message.sender.web;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * @author felfor
 * @since 10/17/17
 */
public class VertxServer {
	private boolean started = false;

	public synchronized void startServer(int port) {
		if (started)
			throw new RuntimeException("The server already started!");
		VertxOptions options = new VertxOptions();
		options.setBlockedThreadCheckInterval(5*60*1000);
		Vertx vertx = Vertx.vertx(options);
		Router router = new WebServiceHandler().addHandlers(vertx);
		HttpServer server = vertx.createHttpServer().requestHandler(router::accept);
		server.listen(port);
		started = true;
	}
}
