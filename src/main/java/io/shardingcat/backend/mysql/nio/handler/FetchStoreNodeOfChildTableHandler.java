
package io.shardingcat.backend.mysql.nio.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.BackendConnection;
import io.shardingcat.backend.datasource.PhysicalDBNode;
import io.shardingcat.cache.CachePool;
import io.shardingcat.config.ShardingCatConfig;
import io.shardingcat.net.mysql.ErrorPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.route.RouteResultsetNode;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.server.parser.ServerParse;

/**
 * company where id=(select company_id from customer where id=3); the one which
 * return data (id) is the datanode to store child table's records
 * 
 * @author wuzhih
 * 
 */
public class FetchStoreNodeOfChildTableHandler implements ResponseHandler {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FetchStoreNodeOfChildTableHandler.class);
	private String sql;
	private volatile String result;
	private volatile String dataNode;
	private AtomicInteger finished = new AtomicInteger(0);
	protected final ReentrantLock lock = new ReentrantLock();
	
	public String execute(String schema, String sql, List<String> dataNodes, ServerConnection sc) {
		
		String key = schema + ":" + sql;
		CachePool cache = ShardingCatServer.getInstance().getCacheService()
				.getCachePool("ER_SQL2PARENTID");
		String result = (String) cache.get(key);
		if (result != null) {
			return result;
		}
		this.sql = sql;
		int totalCount = dataNodes.size();
		long startTime = System.currentTimeMillis();
		long endTime = startTime + 5 * 60 * 1000L;
		ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();

		LOGGER.debug("find child node with sql:" + sql);
		for (String dn : dataNodes) {
			if (dataNode != null) {
				return dataNode;
			}
			PhysicalDBNode mysqlDN = conf.getDataNodes().get(dn);
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("execute in datanode " + dn);
				}
				RouteResultsetNode node = new RouteResultsetNode(dn, ServerParse.SELECT, sql);
				node.setRunOnSlave(false);	// 获取 子表节点，最好走master为好

				/*
				 * fix #1370 默认应该先从已经持有的连接中取连接, 否则可能因为事务隔离性看不到当前事务内更新的数据
				 * Tips: 通过mysqlDN.getConnection获取到的连接不是当前连接
				 *
				 */
				BackendConnection conn = sc.getSession2().getTarget(node);
				if(sc.getSession2().tryExistsCon(conn, node)) {
					_execute(conn, node, sc);
				} else {
					mysqlDN.getConnection(mysqlDN.getDatabase(), sc.isAutocommit(), node, this, node);
				}
			} catch (Exception e) {
				LOGGER.warn("get connection err " + e);
			}
		}

		while (dataNode == null && System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				break;
			}
			if (dataNode != null || finished.get() >= totalCount) {
				break;
			}
		}
		if (dataNode != null) {
			cache.putIfAbsent(key, dataNode);
		}
		return dataNode;
		
	}

	public String execute(String schema, String sql, ArrayList<String> dataNodes) {
		String key = schema + ":" + sql;
		CachePool cache = ShardingCatServer.getInstance().getCacheService()
				.getCachePool("ER_SQL2PARENTID");
		String result = (String) cache.get(key);
		if (result != null) {
			return result;
		}
		this.sql = sql;
		int totalCount = dataNodes.size();
		long startTime = System.currentTimeMillis();
		long endTime = startTime + 5 * 60 * 1000L;
		ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();

		LOGGER.debug("find child node with sql:" + sql);
		for (String dn : dataNodes) {
			if (dataNode != null) {
				return dataNode;
			}
			PhysicalDBNode mysqlDN = conf.getDataNodes().get(dn);
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("execute in datanode " + dn);
				}
				RouteResultsetNode node = new RouteResultsetNode(dn, ServerParse.SELECT, sql);
				node.setRunOnSlave(false);	// 获取 子表节点，最好走master为好

				mysqlDN.getConnection(mysqlDN.getDatabase(), true, node, this, node);
				 
//				mysqlDN.getConnection(mysqlDN.getDatabase(), true,
//						new RouteResultsetNode(dn, ServerParse.SELECT, sql),
//						this, dn);
			} catch (Exception e) {
				LOGGER.warn("get connection err " + e);
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {

			}
		}

		while (dataNode == null && System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				break;
			}
			if (dataNode != null || finished.get() >= totalCount) {
				break;
			}
		}
		if (dataNode != null) {
			cache.putIfAbsent(key, dataNode);
		}
		return dataNode;

	}
	
	private void _execute(BackendConnection conn, RouteResultsetNode node, ServerConnection sc) {
		conn.setResponseHandler(this);
		try {
			conn.execute(node, sc, sc.isAutocommit());
		} catch (IOException e) {
			connectionError(e, conn);
		}
	}

	@Override
	public void connectionAcquired(BackendConnection conn) {
		conn.setResponseHandler(this);
		try {
			conn.query(sql);
		} catch (Exception e) {
			executeException(conn, e);
		}
	}

	@Override
	public void connectionError(Throwable e, BackendConnection conn) {
		finished.incrementAndGet();
		LOGGER.warn("connectionError " + e);

	}

	@Override
	public void errorResponse(byte[] data, BackendConnection conn) {
		finished.incrementAndGet();
		ErrorPacket err = new ErrorPacket();
		err.read(data);
		LOGGER.warn("errorResponse " + err.errno + " "
				+ new String(err.message));
		conn.release();

	}

	@Override
	public void okResponse(byte[] ok, BackendConnection conn) {
		boolean executeResponse = conn.syncAndExcute();
		if (executeResponse) {
			finished.incrementAndGet();
			conn.release();
		}

	}

	@Override
	public void rowResponse(byte[] row, BackendConnection conn) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("received rowResponse response," + getColumn(row)
					+ " from  " + conn);
		}
		if (result == null) {
			result = getColumn(row);
			dataNode = ((RouteResultsetNode) conn.getAttachment()).getName();
		} else {
			LOGGER.warn("find multi data nodes for child table store, sql is:  "
					+ sql);
		}

	}

	private String getColumn(byte[] row) {
		RowDataPacket rowDataPkg = new RowDataPacket(1);
		rowDataPkg.read(row);
		byte[] columnData = rowDataPkg.fieldValues.get(0);
		return new String(columnData);
	}

	@Override
	public void rowEofResponse(byte[] eof, BackendConnection conn) {
		finished.incrementAndGet();
		conn.release();
	}

	private void executeException(BackendConnection c, Throwable e) {
		finished.incrementAndGet();
		LOGGER.warn("executeException   " + e);
		c.close("exception:" + e);

	}

	@Override
	public void writeQueueAvailable() {

	}

	@Override
	public void connectionClose(BackendConnection conn, String reason) {

		LOGGER.warn("connection closed " + conn + " reason:" + reason);
	}

	@Override
	public void fieldEofResponse(byte[] header, List<byte[]> fields,
			byte[] eof, BackendConnection conn) {

	}

}