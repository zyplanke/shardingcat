
package io.shardingcat.backend.mysql.nio.handler;

import java.util.List;

import io.shardingcat.backend.mysql.nio.MySQLConnection;
import io.shardingcat.config.ErrorCode;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.backend.BackendConnection;
import io.shardingcat.route.RouteResultsetNode;
import io.shardingcat.server.NonBlockingSession;

/**
 * @author shardingcat
 */
public class RollbackNodeHandler extends MultiNodeHandler {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RollbackNodeHandler.class);

	public RollbackNodeHandler(NonBlockingSession session) {
		super(session);
	}

	public void rollback() {
		final int initCount = session.getTargetCount();
		lock.lock();
		try {
			reset(initCount);
		} finally {
			lock.unlock();
		}
		if (session.closed()) {
			decrementCountToZero();
			return;
		}

		// 执行
		int started = 0;
		for (final RouteResultsetNode node : session.getTargetKeys()) {
			if (node == null) {
					LOGGER.error("null is contained in RoutResultsetNodes, source = "
							+ session.getSource());
				continue;
			}
			final BackendConnection conn = session.getTarget(node);

			if (conn != null) {
				boolean isClosed=conn.isClosedOrQuit();
				    if(isClosed)
					{
						session.getSource().writeErrMessage(ErrorCode.ER_UNKNOWN_ERROR,
								"receive rollback,but find backend con is closed or quit");
						LOGGER.error( conn+"receive rollback,but fond backend con is closed or quit");
					}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("rollback job run for " + conn);
				}
				if (clearIfSessionClosed(session)) {
					return;
				}
				conn.setResponseHandler(RollbackNodeHandler.this);

				//support the XA rollback
				if(session.getXaTXID()!=null && conn instanceof  MySQLConnection) {
					MySQLConnection mysqlCon = (MySQLConnection) conn;
					String xaTxId = session.getXaTXID() +",'"+ mysqlCon.getSchema()+"'";
					//exeBatch cmd issue : the 2nd package can not receive the response
					mysqlCon.execCmd("XA END " + xaTxId  + ";");
					mysqlCon.execCmd("XA ROLLBACK " + xaTxId + ";");
				}else {
					conn.rollback();
				}


				++started;
			}
		}

		if (started < initCount && decrementCountBy(initCount - started)) {
			/**
			 * assumption: only caused by front-end connection close. <br/>
			 * Otherwise, packet must be returned to front-end
			 */
			session.clearResources(true);
		}
	}

	@Override
	public void okResponse(byte[] ok, BackendConnection conn) {
		if (decrementCountBy(1)) {
			// clear all resources
			session.clearResources(false);
			if (this.isFail() || session.closed()) {
				tryErrorFinished(true);
			} else {
				/* 1.  事务结束后,xa事务结束    */
				if(session.getXaTXID()!=null){
					session.setXATXEnabled(false);
				}
				
				/* 2. preAcStates 为true,事务结束后,需要设置为true。preAcStates 为ac上一个状态    */
		        if(session.getSource().isPreAcStates()&&!session.getSource().isAutocommit()){
		        	session.getSource().setAutocommit(true);
		        }
		        
				session.getSource().write(ok);
			}
		}
	}

	@Override
	public void rowEofResponse(byte[] eof, BackendConnection conn) {
		LOGGER.error(new StringBuilder().append("unexpected packet for ")
				.append(conn).append(" bound by ").append(session.getSource())
				.append(": field's eof").toString());
	}

	@Override
	public void connectionAcquired(BackendConnection conn) {
		LOGGER.error("unexpected invocation: connectionAcquired from rollback");
	}

	@Override
	public void fieldEofResponse(byte[] header, List<byte[]> fields,
			byte[] eof, BackendConnection conn) {
		LOGGER.error(new StringBuilder().append("unexpected packet for ")
				.append(conn).append(" bound by ").append(session.getSource())
				.append(": field's eof").toString());
	}

	@Override
	public void rowResponse(byte[] row, BackendConnection conn) {
		LOGGER.error(new StringBuilder().append("unexpected packet for ")
				.append(conn).append(" bound by ").append(session.getSource())
				.append(": field's eof").toString());
	}

	@Override
	public void writeQueueAvailable() {

	}

}
