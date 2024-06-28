
package io.shardingcat.net;

import java.nio.channels.CompletionHandler;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shardingcat
 */
public final class AIOConnector implements SocketConnector,
		CompletionHandler<Void, BackendAIOConnection> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AIOConnector.class);
	private static final ConnectIdGenerator ID_GENERATOR = new ConnectIdGenerator();

	public AIOConnector() {

	}

	@Override
	public void completed(Void result, BackendAIOConnection attachment) {
		finishConnect(attachment);
	}

	@Override
	public void failed(Throwable exc, BackendAIOConnection conn) {
		conn.onConnectFailed(exc);
	}

	private void finishConnect(BackendAIOConnection c) {
		try {
			if (c.finishConnect()) {
				c.setId(ID_GENERATOR.getId());
				NIOProcessor processor = ShardingCatServer.getInstance()
						.nextProcessor();
				c.setProcessor(processor);
				c.register();
			}
		} catch (Exception e) {
			c.onConnectFailed(e);
			LOGGER.info("connect err " , e);
			c.close(e.toString());
		}
	}

	/**
	 * 后端连接ID生成器
	 * 
	 * @author shardingcat
	 */
	private static class ConnectIdGenerator {

		private static final long MAX_VALUE = Long.MAX_VALUE;

		private AtomicLong connectId = new AtomicLong(0);

		private long getId() {
			return connectId.incrementAndGet();
		}
	}
}
