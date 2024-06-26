package io.shardingcat.backend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.shardingcat.backend.mysql.nio.handler.ResponseHandler;
import io.shardingcat.net.ClosableConnection;
import io.shardingcat.route.RouteResultsetNode;
import io.shardingcat.server.ServerConnection;

public interface BackendConnection extends ClosableConnection {
	public boolean isModifiedSQLExecuted();

	public boolean isFromSlaveDB();

	public String getSchema();

	public void setSchema(String newSchema);

	public long getLastTime();

	public boolean isClosedOrQuit();

	public void setAttachment(Object attachment);

	public void quit();

	public void setLastTime(long currentTimeMillis);

	public void release();

	public boolean setResponseHandler(ResponseHandler commandHandler);

	public void commit();

	public void query(String sql) throws UnsupportedEncodingException;

	public Object getAttachment();

	// public long getThreadId();



	public void execute(RouteResultsetNode node, ServerConnection source,
			boolean autocommit) throws IOException;

	public void recordSql(String host, String schema, String statement);

	public boolean syncAndExcute();

	public void rollback();

	public boolean isBorrowed();

	public void setBorrowed(boolean borrowed);

	public int getTxIsolation();

	public boolean isAutocommit();

	public long getId();

	public void discardClose(String reason);

}
