package io.shardingcat.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.config.ErrorCode;
import io.shardingcat.route.parser.druid.DruidSequenceHandler;

public class ShardingCatSequnceProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShardingCatSequnceProcessor.class);
	
	//使用Druid解析器实现sequence处理  @兵临城下
	private static final DruidSequenceHandler sequenceHandler = new DruidSequenceHandler(ShardingCatServer
			.getInstance().getConfig().getSystem().getSequnceHandlerType());
	
	private static class InnerShardingCatSequnceProcessor{
		private static ShardingCatSequnceProcessor INSTANCE = new ShardingCatSequnceProcessor();
	}
	
	public static ShardingCatSequnceProcessor getInstance(){
		return InnerShardingCatSequnceProcessor.INSTANCE;
	}
	
	private ShardingCatSequnceProcessor() {
	}

	/**
	 *  锁的粒度控制到序列级别.一个序列一把锁.
	 *  如果是 db 方式, 可以 给 shardingcat_sequence表的 name 列 加索引.可以借助mysql 行级锁 提高并发
	 * @param pair
	 */
	public void executeSeq(SessionSQLPair pair) {
		try {
			/*// @micmiu 扩展NodeToString实现自定义全局序列号
			NodeToString strHandler = new ExtNodeToString4SEQ(ShardingCatServer
					.getInstance().getConfig().getSystem()
					.getSequnceHandlerType());
			// 如果存在sequence 转化sequence为实际数值
			String charset = pair.session.getSource().getCharset();
			QueryTreeNode ast = SQLParserDelegate.parse(pair.sql,
					charset == null ? "utf-8" : charset);
			String sql = strHandler.toString(ast);
			if (sql.toUpperCase().startsWith("SELECT")) {
				String value=sql.substring("SELECT".length()).trim();
				outRawData(pair.session.getSource(),value);
				return;
			}*/
			
			String charset = pair.session.getSource().getCharset();
			String executeSql = sequenceHandler.getExecuteSql(pair,charset == null ? "utf-8":charset);
			
			pair.session.getSource().routeEndExecuteSQL(executeSql, pair.type,pair.schema);
		} catch (Exception e) {
			LOGGER.error("ShardingCatSequenceProcessor.executeSeq(SesionSQLPair)",e);
			pair.session.getSource().writeErrMessage(ErrorCode.ER_YES,"shardingcat sequnce err." + e);
			return;
		}
	}
}
