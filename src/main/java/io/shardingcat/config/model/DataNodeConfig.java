
package io.shardingcat.config.model;

/**
 * 用于描述一个数据节点的配置
 * 
 * @author shardingcat
 */
public final class DataNodeConfig {

	private final String name;
	private final String database;
	private final String dataHost;

	public DataNodeConfig(String name, String database, String dataHost) {
		super();
		this.name = name;
		this.database = database;
		this.dataHost = dataHost;
	}

	public String getName() {
		return name;
	}

	public String getDatabase() {
		return database;
	}

	public String getDataHost() {
		return dataHost;
	}

}