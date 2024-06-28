
package io.shardingcat.config.loader.xml;

import java.util.Map;

import io.shardingcat.config.loader.ConfigLoader;
import io.shardingcat.config.loader.SchemaLoader;
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
public class XMLConfigLoader implements ConfigLoader {

    /** unmodifiable */
    private final Map<String, DataHostConfig> dataHosts;
    /** unmodifiable */
    private final Map<String, DataNodeConfig> dataNodes;
    /** unmodifiable */
    private final Map<String, SchemaConfig> schemas;
    private final SystemConfig system;
    /** unmodifiable */
    private final Map<String, UserConfig> users;
    private final FirewallConfig firewall;
    private final ClusterConfig cluster;

    public XMLConfigLoader(SchemaLoader schemaLoader) {
        XMLServerLoader serverLoader = new XMLServerLoader();
        this.system = serverLoader.getSystem();
        this.users = serverLoader.getUsers();
        this.firewall = serverLoader.getFirewall();
        this.cluster = serverLoader.getCluster();
        this.dataHosts = schemaLoader.getDataHosts();
        this.dataNodes = schemaLoader.getDataNodes();
        this.schemas = schemaLoader.getSchemas();
        schemaLoader = null;
    }

    @Override
    public ClusterConfig getClusterConfig() {
        return cluster;
    }

    @Override
    public FirewallConfig getFirewallConfig() {
        return firewall;
    }

    @Override
    public UserConfig getUserConfig(String user) {
        return users.get(user);
    }

    @Override
    public Map<String, UserConfig> getUserConfigs() {
        return users;
    }

    @Override
    public SystemConfig getSystemConfig() {
        return system;
    }
    @Override
    public Map<String, SchemaConfig> getSchemaConfigs() {
        return schemas;
    }

    @Override
    public Map<String, DataNodeConfig> getDataNodes() {
        return dataNodes;
    }

    @Override
    public Map<String, DataHostConfig> getDataHosts() {
        return dataHosts;
    }

    @Override
    public SchemaConfig getSchemaConfig(String schema) {
        return schemas.get(schema);
    }

}