
package io.shardingcat.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.shardingcat.config.model.ClusterConfig;
import io.shardingcat.config.model.ShardingCatNodeConfig;

/**
 * @author shardingcat
 */
public final class ShardingCatCluster {

    private final Map<String, ShardingCatNode> nodes;
    private final Map<String, List<String>> groups;

    public ShardingCatCluster(ClusterConfig clusterConf) {
        this.nodes = new HashMap<String, ShardingCatNode>(clusterConf.getNodes().size());
        this.groups = clusterConf.getGroups();
        for (ShardingCatNodeConfig conf : clusterConf.getNodes().values()) {
            String name = conf.getName();
            ShardingCatNode node = new ShardingCatNode(conf);
            this.nodes.put(name, node);
        }
    }

    public Map<String, ShardingCatNode> getNodes() {
        return nodes;
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }

}