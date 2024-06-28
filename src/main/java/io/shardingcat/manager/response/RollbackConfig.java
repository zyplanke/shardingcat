
package io.shardingcat.manager.response;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.datasource.PhysicalDBNode;
import io.shardingcat.backend.datasource.PhysicalDBPool;
import io.shardingcat.config.ErrorCode;
import io.shardingcat.config.ShardingCatCluster;
import io.shardingcat.config.ShardingCatConfig;
import io.shardingcat.config.model.FirewallConfig;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.UserConfig;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.OkPacket;

/**
 * @author shardingcat
 */
public final class RollbackConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(RollbackConfig.class);

	public static void execute(ManagerConnection c) {
		final ReentrantLock lock = ShardingCatServer.getInstance().getConfig()
				.getLock();
		lock.lock();
		try {
			if (rollback()) {
				StringBuilder s = new StringBuilder();
				s.append(c).append("Rollback config success by manager");
				LOGGER.warn(s.toString());
				OkPacket ok = new OkPacket();
				ok.packetId = 1;
				ok.affectedRows = 1;
				ok.serverStatus = 2;
				ok.message = "Rollback config success".getBytes();
				ok.write(c);
			} else {
				c.writeErrMessage(ErrorCode.ER_YES, "Rollback config failure");
			}
		} finally {
			lock.unlock();
		}
	}

	private static boolean rollback() {
		ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
		Map<String, UserConfig> users = conf.getBackupUsers();
		Map<String, SchemaConfig> schemas = conf.getBackupSchemas();
		Map<String, PhysicalDBNode> dataNodes = conf.getBackupDataNodes();
		Map<String, PhysicalDBPool> dataHosts = conf.getBackupDataHosts();
		ShardingCatCluster cluster = conf.getBackupCluster();
		FirewallConfig firewall = conf.getBackupFirewall();

		// 检查可回滚状态
		if (!conf.canRollback()) {
			return false;
		}

		// 如果回滚已经存在的pool
		boolean rollbackStatus = true;
		Map<String, PhysicalDBPool> cNodes = conf.getDataHosts();
		for (PhysicalDBPool dn : dataHosts.values()) {
			dn.init(dn.getActivedIndex());
			if (!dn.isInitSuccess()) {
				rollbackStatus = false;
				break;
			}
		}
		// 如果回滚不成功，则清理已初始化的资源。
		if (!rollbackStatus) {
			for (PhysicalDBPool dn : dataHosts.values()) {
				dn.clearDataSources("rollbackup config");
				dn.stopHeartbeat();
			}
			return false;
		}

		// 应用回滚
		conf.rollback(users, schemas, dataNodes, dataHosts, cluster, firewall);

		// 处理旧的资源
		for (PhysicalDBPool dn : cNodes.values()) {
			dn.clearDataSources("clear old config ");
			dn.stopHeartbeat();
		}

		//清理缓存
		 ShardingCatServer.getInstance().getCacheService().clearCache();
		return true;
	}

}