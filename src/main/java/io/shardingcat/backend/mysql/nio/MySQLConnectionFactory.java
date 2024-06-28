
package io.shardingcat.backend.mysql.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NetworkChannel;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.mysql.nio.handler.ResponseHandler;
import io.shardingcat.config.model.DBHostConfig;
import io.shardingcat.net.NIOConnector;
import io.shardingcat.net.factory.BackendConnectionFactory;

/**
 * @author shardingcat
 */
public class MySQLConnectionFactory extends BackendConnectionFactory {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MySQLConnection make(MySQLDataSource pool, ResponseHandler handler,
			String schema) throws IOException {

		DBHostConfig dsc = pool.getConfig();
		NetworkChannel channel = openSocketChannel(ShardingCatServer.getInstance()
				.isAIO());

		MySQLConnection c = new MySQLConnection(channel, pool.isReadNode());
		ShardingCatServer.getInstance().getConfig().setSocketParams(c, false);
		c.setHost(dsc.getIp());
		c.setPort(dsc.getPort());
		c.setUser(dsc.getUser());
		c.setPassword(dsc.getPassword());
		c.setSchema(schema);
		c.setHandler(new MySQLConnectionAuthenticator(c, handler));
		c.setPool(pool);
		c.setIdleTimeout(pool.getConfig().getIdleTimeout());
		if (channel instanceof AsynchronousSocketChannel) {
			((AsynchronousSocketChannel) channel).connect(
					new InetSocketAddress(dsc.getIp(), dsc.getPort()), c,
					(CompletionHandler) ShardingCatServer.getInstance()
							.getConnector());
		} else {
			((NIOConnector) ShardingCatServer.getInstance().getConnector())
					.postConnect(c);

		}
		return c;
	}

}