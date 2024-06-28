
package io.shardingcat.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NetworkChannel;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.net.factory.FrontendConnectionFactory;

/**
 * @author shardingcat
 */
public final class AIOAcceptor implements SocketAcceptor,
		CompletionHandler<AsynchronousSocketChannel, Long> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AIOAcceptor.class);
	private static final AcceptIdGenerator ID_GENERATOR = new AcceptIdGenerator();

	private final int port;
	private final AsynchronousServerSocketChannel serverChannel;
	private final FrontendConnectionFactory factory;

	private long acceptCount;
	private final String name;

	public AIOAcceptor(String name, String ip, int port,
			FrontendConnectionFactory factory, AsynchronousChannelGroup group)
			throws IOException {
		this.name = name;
		this.port = port;
		this.factory = factory;
		serverChannel = AsynchronousServerSocketChannel.open(group);
		/** 设置TCP属性 */
		serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 16 * 2);
		// backlog=100
		serverChannel.bind(new InetSocketAddress(ip, port), 100);
	}

	public String getName() {
		return name;
	}

	public void start() {
		this.pendingAccept();
	}

	public int getPort() {
		return port;
	}

	public long getAcceptCount() {
		return acceptCount;
	}

	private void accept(NetworkChannel channel, Long id) {
		try {
			FrontendConnection c = factory.make(channel);
			c.setAccepted(true);
			c.setId(id);
			NIOProcessor processor = ShardingCatServer.getInstance().nextProcessor();
			c.setProcessor(processor);
			c.register();
		} catch (Exception e) {
		    LOGGER.error("AioAcceptorError", e);
			closeChannel(channel);
		}
	}

	private void pendingAccept() {
		if (serverChannel.isOpen()) {
			serverChannel.accept(ID_GENERATOR.getId(), this);
		} else {
			throw new IllegalStateException(
					"ShardingCat Server Channel has been closed");
		}

	}

	@Override
	public void completed(AsynchronousSocketChannel result, Long id) {
		accept(result, id);
		// next pending waiting
		pendingAccept();

	}

	@Override
	public void failed(Throwable exc, Long id) {
		LOGGER.info("acception connect failed:" + exc);
		// next pending waiting
		pendingAccept();

	}

	private static void closeChannel(NetworkChannel channel) {
		if (channel == null) {
			return;
		}
		try {
			channel.close();
		} catch (IOException e) {
	        LOGGER.error("AioAcceptorError", e);
		}
	}

	/**
	 * 前端连接ID生成器
	 * 
	 * @author shardingcat
	 */
	private static class AcceptIdGenerator {

		private static final long MAX_VALUE = 0xffffffffL;

		private AtomicLong acceptId = new AtomicLong();
		private final Object lock = new Object();

		private long getId() {
			long newValue = acceptId.getAndIncrement();
			if (newValue >= MAX_VALUE) {
				synchronized (lock) {
					newValue = acceptId.getAndIncrement();
					if (newValue >= MAX_VALUE) {
						acceptId.set(0);
					}
				}
				return acceptId.getAndDecrement();
			} else {
				return newValue;
			}
		}
	}
}
