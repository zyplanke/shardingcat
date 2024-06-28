
package io.shardingcat.config.loader;

import java.util.Map;

import io.shardingcat.config.model.ClusterConfig;
import io.shardingcat.config.model.DataHostConfig;
import io.shardingcat.config.model.DataNodeConfig;
import io.shardingcat.config.model.FirewallConfig;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.config.model.UserConfig;

/**
 * @author shardingcat
 */
public interface ConfigLoader {
	SchemaConfig getSchemaConfig(String schema);

	Map<String, SchemaConfig> getSchemaConfigs();

	Map<String, DataNodeConfig> getDataNodes();

	Map<String, DataHostConfig> getDataHosts();

	SystemConfig getSystemConfig();

	UserConfig getUserConfig(String user);

	Map<String, UserConfig> getUserConfigs();

	FirewallConfig getFirewallConfig();

	ClusterConfig getClusterConfig();
}