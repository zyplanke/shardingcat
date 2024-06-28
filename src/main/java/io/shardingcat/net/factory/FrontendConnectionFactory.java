
package io.shardingcat.net.factory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.NetworkChannel;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.net.FrontendConnection;

/**
 * @author shardingcat
 */
public abstract class FrontendConnectionFactory {
	protected abstract FrontendConnection getConnection(NetworkChannel channel)
			throws IOException;

	public FrontendConnection make(NetworkChannel channel) throws IOException {
		channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

		FrontendConnection c = getConnection(channel);
		ShardingCatServer.getInstance().getConfig().setSocketParams(c, true);
		return c;
	}

	

}