
package io.shardingcat.backend.mysql.nio.handler;

import java.util.List;

import io.shardingcat.backend.mysql.xa.TxState;
import io.shardingcat.config.ErrorCode;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.backend.BackendConnection;
import io.shardingcat.backend.mysql.nio.MySQLConnection;
import io.shardingcat.net.mysql.ErrorPacket;
import io.shardingcat.server.NonBlockingSession;
import io.shardingcat.server.ServerConnection;

/**
 * @author shardingcat
 */
public class CommitNodeHandler implements ResponseHandler {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CommitNodeHandler.class);
	private final NonBlockingSession session;

	public CommitNodeHandler(NonBlockingSession session) {
		this.session = session;
	}

	public void commit(BackendConnection conn) {
		conn.setResponseHandler(CommitNodeHandler.this);
		boolean isClosed=conn.isClosedOrQuit();
		if(isClosed)
		{
			session.getSource().writeErrMessage(ErrorCode.ER_UNKNOWN_ERROR,
					"receive commit,but find backend con is closed or quit");
			LOGGER.error( conn+"receive commit,but fond backend con is closed or quit");
		}
	   if(conn instanceof MySQLConnection)
	   {
		   MySQLConnection mysqlCon = (MySQLConnection) conn;
		   if (mysqlCon.getXaStatus() == 1)
		   {
			   String xaTxId = session.getXaTXID()+",'"+mysqlCon.getSchema()+"'";
			   String[] cmds = new String[]{"XA END " + xaTxId,
					   "XA PREPARE " + xaTxId};
			   mysqlCon.execBatchCmd(cmds);
		   } else
		   {
			   conn.commit();
		   }
	   }else
	   {
		   conn.commit();
	   }
	}

	@Override
	public void connectionAcquired(BackendConnection conn) {
		LOGGER.error("unexpected invocation: connectionAcquired from commit");

	}

	@Override
	public void okResponse(byte[] ok, BackendConnection conn) {
		if(conn instanceof MySQLConnection)
		{
			MySQLConnection mysqlCon = (MySQLConnection) conn;
			switch (mysqlCon.getXaStatus())
			{
				case TxState.TX_STARTED_STATE:
					if (mysqlCon.batchCmdFinished())
					{
						String xaTxId = session.getXaTXID()+",'"+mysqlCon.getSchema()+"'";
						mysqlCon.execCmd("XA COMMIT " + xaTxId);
						mysqlCon.setXaStatus(TxState.TX_PREPARED_STATE);
					}
					return;
				case TxState.TX_PREPARED_STATE:
				{
					mysqlCon.setXaStatus(TxState.TX_INITIALIZE_STATE);
					break;
				}
				default:
				//	LOGGER.error("Wrong XA status flag!");
			}
			
			/* 1.  事务提交后,xa 事务结束     */
			if(TxState.TX_INITIALIZE_STATE==mysqlCon.getXaStatus()){
				if(session.getXaTXID()!=null){
					session.setXATXEnabled(false);
				}
			}
		}
		
		/* 2. preAcStates 为true,事务结束后,需要设置为true。preAcStates 为ac上一个状态    */
        if(session.getSource().isPreAcStates()&&!session.getSource().isAutocommit()){
        	session.getSource().setAutocommit(true);
        }
		session.clearResources(false);
		ServerConnection source = session.getSource();
		source.write(ok);
	}

	@Override
	public void errorResponse(byte[] err, BackendConnection conn) {
		ErrorPacket errPkg = new ErrorPacket();
		errPkg.read(err);
		String errInfo = new String(errPkg.message);
		session.getSource().setTxInterrupt(errInfo);
		errPkg.write(session.getSource());

	}

	@Override
	public void rowEofResponse(byte[] eof, BackendConnection conn) {
		LOGGER.error(new StringBuilder().append("unexpected packet for ")
				.append(conn).append(" bound by ").append(session.getSource())
				.append(": field's eof").toString());
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
		LOGGER.warn(new StringBuilder().append("unexpected packet for ")
				.append(conn).append(" bound by ").append(session.getSource())
				.append(": row data packet").toString());
	}

	@Override
	public void writeQueueAvailable() {

	}

	@Override
	public void connectionError(Throwable e, BackendConnection conn) {
		

	}

	@Override
	public void connectionClose(BackendConnection conn, String reason) {
		

	}

}
