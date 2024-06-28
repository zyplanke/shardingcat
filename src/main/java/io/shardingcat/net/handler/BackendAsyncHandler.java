
package io.shardingcat.net.handler;

import java.util.concurrent.Executor;

import io.shardingcat.net.NIOHandler;

/**
 * @author shardingcat
 */
public abstract class BackendAsyncHandler implements NIOHandler {

	protected void offerData(byte[] data, Executor executor) {
		handleData(data);

		// if (dataQueue.offer(data)) {
		// handleQueue(executor);
		// } else {
		// offerDataError();
		// }
	}

	protected abstract void offerDataError();

	protected abstract void handleData(byte[] data);

}