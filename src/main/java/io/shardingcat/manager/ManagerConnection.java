
package io.shardingcat.manager;

import java.io.IOException;
import java.nio.channels.NetworkChannel;

import io.shardingcat.net.FrontendConnection;
import io.shardingcat.util.TimeUtil;

/**
 * @author shardingcat
 */
public class ManagerConnection extends FrontendConnection {
	private static final long AUTH_TIMEOUT = 15 * 1000L;

	public ManagerConnection(NetworkChannel channel) throws IOException {
		super(channel);
	}

	@Override
	public boolean isIdleTimeout() {
		if (isAuthenticated) {
			return super.isIdleTimeout();
		} else {
			return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime,
					lastReadTime) + AUTH_TIMEOUT;
		}
	}

	@Override
	public void handle(final byte[] data) {
		handler.handle(data);
	}

}