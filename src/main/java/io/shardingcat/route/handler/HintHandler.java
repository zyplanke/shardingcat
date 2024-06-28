package io.shardingcat.route.handler;

import java.sql.SQLNonTransientException;
import java.util.Map;

import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.route.RouteResultset;
import io.shardingcat.server.ServerConnection;

/**
 * 按照注释中包含指定类型的内容做路由解析
 * 
 */
public interface HintHandler {

	public RouteResultset route(SystemConfig sysConfig, SchemaConfig schema,
                                int sqlType, String realSQL, String charset, ServerConnection sc,
                                LayerCachePool cachePool, String hintSQLValue, int hintSqlType, Map hintMap)
			throws SQLNonTransientException;
}
