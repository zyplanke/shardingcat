
package io.shardingcat.config;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.config.model.ShardingCatNodeConfig;

/**
 * @author shardingcat
 */
public class ShardingCatNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShardingCatNode.class);

	private final String name;
	private final ShardingCatNodeConfig config;

	public ShardingCatNode(ShardingCatNodeConfig config) {
		this.name = config.getName();
		this.config = config;
	}

	public String getName() {
		return name;
	}

	public ShardingCatNodeConfig getConfig() {
		return config;
	}

	public boolean isOnline() {
		return (true);
	}

}