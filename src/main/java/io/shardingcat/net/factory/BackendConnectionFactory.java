
package io.shardingcat.net.factory;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;

import io.shardingcat.ShardingCatServer;

/**
 * @author shardingcat
 */
public abstract class BackendConnectionFactory {

	protected NetworkChannel openSocketChannel(boolean isAIO)
			throws IOException {
		if (isAIO) {
			return AsynchronousSocketChannel
                .open(ShardingCatServer.getInstance().getNextAsyncChannelGroup());
		} else {
			SocketChannel channel = null;
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			return channel;
		}

	}

}