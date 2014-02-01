package jnode.protocol.binkp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.main.threads.ThreadPool;
import jnode.protocol.binkp.connector.BinkpAsyncConnector;

public class BinkpAsyncServer implements Runnable {
	private static final Logger logger = Logger
			.getLogger(BinkpAsyncServer.class);

	private static final String BINKD_BIND = "binkp.bind";
	private static final String BINKD_PORT = "binkp.port";
	private static final String BINKD_SERVER = "binkp.server";

	@Override
	public void run() {
		if (!MainHandler.getCurrentInstance().getBooleanProperty(BINKD_SERVER,
				true)) {
			return;
		}
		try {
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			InetSocketAddress bind = new InetSocketAddress(MainHandler
					.getCurrentInstance().getProperty(BINKD_BIND, "0.0.0.0"),
					MainHandler.getCurrentInstance().getIntegerProperty(
							BINKD_PORT, 24554));
			server.bind(bind, 5);
			logger.l1("We are listening on " + bind.getHostString() + ":"
					+ bind.getPort());
			Selector selector = Selector.open();
			server.register(selector, server.validOps());
			while (true) {
				selector.select();
				for (SelectionKey key : selector.selectedKeys()) {
					try {
						ServerSocketChannel channel = (ServerSocketChannel) key
								.channel();
						if (key.isValid()) {
							if (key.isAcceptable()) {
								SocketChannel client = channel.accept();
								InetSocketAddress addr = (InetSocketAddress) client
										.getRemoteAddress();
								logger.l2(String.format(
										"Incoming connection from %s:%d",
										addr.getHostString(), addr.getPort()));
								ThreadPool.execute(BinkpAsyncConnector
										.accept(client));
							}
						}
					} catch (IOException e) {
						logger.l2("Error in accept(): " + e.getLocalizedMessage());
					} catch(RuntimeException e) {
						logger.l2("RuntimeException: " + e.getLocalizedMessage(), e);
					}
				}
			}
		} catch (IOException e) {
			logger.l1("Server error occured!", e);
		}
	}
}
