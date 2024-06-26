package io.shardingcat.route.handler;

import java.sql.SQLNonTransientException;
import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.catlets.Catlet;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.route.RouteResultset;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.sqlengine.EngineCtx;

/**
 * 处理注释中类型为catlet 的情况,每个catlet为一个用户自定义Java代码类，用于进行复杂查询SQL（只能是查询SQL）的自定义执行过程，
 * 目前主要用于跨分片Join的人工智能编码
 */
public class HintCatletHandler implements HintHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(HintCatletHandler.class);

	/**
	 * 从全局的schema列表中查询指定的schema是否存在， 如果存在则替换connection属性中原有的schema，
	 * 如果不存在，则throws SQLNonTransientException，表示指定的schema 不存在
	 * 
	 * @param sysConfig
	 * @param schema
	 * @param sqlType
	 * @param realSQL
	 * @param charset
	 * @param info
	 * @param cachePool
	 * @param hintSQLValue
	 * @return
	 * @throws SQLNonTransientException
	 */
	@Override
	public RouteResultset route(SystemConfig sysConfig, SchemaConfig schema,
			int sqlType, String realSQL, String charset, ServerConnection sc,
			LayerCachePool cachePool, String hintSQLValue,int hintSqlType, Map hintMap)
			throws SQLNonTransientException {
		// sc.setEngineCtx ctx
		String cateletClass = hintSQLValue;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("load catelet class:" + hintSQLValue + " to run sql "
					+ realSQL);
		}
		try {
			Catlet catlet = (Catlet) ShardingCatServer.getInstance()
					.getCatletClassLoader().getInstanceofClass(cateletClass);
			catlet.route(sysConfig, schema, sqlType, realSQL,charset, sc, cachePool);
			catlet.processSQL(realSQL, new EngineCtx(sc.getSession2()));
		} catch (Exception e) {
			LOGGER.warn("catlet error "+e);
			throw new SQLNonTransientException(e);
		}
		return null;
	}
}
