
package io.shardingcat.net;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.shardingcat.util.SelectorUtil;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/**
 * 网络事件反应器
 * 
 * <p>
 * Catch exceptions such as OOM so that the reactor can keep running for response client!
 * </p>
 * @since 2016-03-30
 * 
 * @author shardingcat, Uncle-pan
 * 
 */
public final class NIOReactor {
	private static final Logger LOGGER = LoggerFactory.getLogger(NIOReactor.class);
	private final String name;
	private final RW reactorR;

	public NIOReactor(String name) throws IOException {
		this.name = name;
		this.reactorR = new RW();
	}

	final void startup() {
		new Thread(reactorR, name + "-RW").start();
	}

	final void postRegister(AbstractConnection c) {
		reactorR.registerQueue.offer(c);
		reactorR.selector.wakeup();
	}

	final Queue<AbstractConnection> getRegisterQueue() {
		return reactorR.registerQueue;
	}

	final long getReactCount() {
		return reactorR.reactCount;
	}

	private final class RW implements Runnable {
		private volatile Selector selector;
		private final ConcurrentLinkedQueue<AbstractConnection> registerQueue;
		private long reactCount;

		private RW() throws IOException {
			this.selector = Selector.open();
			this.registerQueue = new ConcurrentLinkedQueue<AbstractConnection>();
		}

		@Override
		public void run() {
			int invalidSelectCount = 0;
			Set<SelectionKey> keys = null;
			for (;;) {
				++reactCount;
				try {
					final Selector tSelector = this.selector;
					long start = System.nanoTime();
					tSelector.select(500L);
					long end = System.nanoTime();
					register(tSelector);
					keys = tSelector.selectedKeys();
					if (keys.size() == 0 && (end - start) < SelectorUtil.MIN_SELECT_TIME_IN_NANO_SECONDS )
					{
						invalidSelectCount++;
					}
					else
					{
						invalidSelectCount = 0;
						for (SelectionKey key : keys) {
							AbstractConnection con = null;
							try {
								Object att = key.attachment();
								if (att != null) {
									con = (AbstractConnection) att;
									if (key.isValid() && key.isReadable()) {
										try {
											con.asynRead();
										} catch (IOException e) {
											con.close("program err:" + e.toString());
											continue;
										} catch (Exception e) {
											LOGGER.warn("caught err:", e);
											con.close("program err:" + e.toString());
											continue;
										}
									}
									if (key.isValid() && key.isWritable()) {
										con.doNextWriteCheck();
									}
								} else {
									key.cancel();
								}
							} catch (CancelledKeyException e) {
								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug(con + " socket key canceled");
								}
							} catch (Exception e) {
								LOGGER.warn(con + " " + e);
							} catch (final Throwable e) {
								// Catch exceptions such as OOM and close connection if exists
								//so that the reactor can keep running!
								// @author Uncle-pan
								// @since 2016-03-30
								if (con != null) {
									con.close("Bad: " + e);
								}
								LOGGER.error("caught err: ", e);
								continue;
							}
						}
					}
					if (invalidSelectCount > SelectorUtil.REBUILD_COUNT_THRESHOLD)
					{
						final Selector rebuildSelector = SelectorUtil.rebuildSelector(this.selector);
						if (rebuildSelector != null)
						{
							this.selector = rebuildSelector;
						}
						invalidSelectCount = 0;
					}
				} catch (Exception e) {
					LOGGER.warn(name, e);
				} catch (final Throwable e){
					// Catch exceptions such as OOM so that the reactor can keep running!
                	// @author Uncle-pan
                	// @since 2016-03-30
					LOGGER.error("caught err: ", e);
				} finally {
					if (keys != null) {
						keys.clear();
					}

				}
			}
		}

		private void register(Selector selector) {
			AbstractConnection c = null;
			if (registerQueue.isEmpty()) {
				return;
			}
			while ((c = registerQueue.poll()) != null) {
				try {
					((NIOSocketWR) c.getSocketWR()).register(selector);
					c.register();
				} catch (Exception e) {
					c.close("register err" + e.toString());
				}
			}
		}

	}

}
