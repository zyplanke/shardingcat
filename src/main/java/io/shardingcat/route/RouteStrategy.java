package io.shardingcat.route;

import java.sql.SQLNonTransientException;

import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.server.ServerConnection;

/**
 * 路由策略接口
 * @author wang.dw
 *
 */
public interface RouteStrategy {
	public RouteResultset route(SystemConfig sysConfig,
			SchemaConfig schema,int sqlType, String origSQL, String charset, ServerConnection sc, LayerCachePool cachePool)
			throws SQLNonTransientException;
}
