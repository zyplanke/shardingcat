
package io.shardingcat.config.loader;

import java.util.Map;

import io.shardingcat.config.model.DataHostConfig;
import io.shardingcat.config.model.DataNodeConfig;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.rule.TableRuleConfig;

/**
 * @author shardingcat
 */
public interface SchemaLoader {
	
    Map<String, TableRuleConfig> getTableRules();

    Map<String, DataHostConfig> getDataHosts();

    Map<String, DataNodeConfig> getDataNodes();

    Map<String, SchemaConfig> getSchemas();

}