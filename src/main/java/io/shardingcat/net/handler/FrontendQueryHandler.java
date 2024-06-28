
package io.shardingcat.net.handler;

/**
 * 查询处理器
 * 
 * @author shardingcat
 */
public interface FrontendQueryHandler {

	void query(String sql);

	void setReadOnly(Boolean readOnly);
}