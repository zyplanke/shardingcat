
package io.shardingcat.route.perf;

import java.sql.SQLNonTransientException;

import io.shardingcat.SimpleCachePool;
import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.loader.SchemaLoader;
import io.shardingcat.config.loader.xml.XMLSchemaLoader;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.route.factory.RouteStrategyFactory;

/**
 * @author shardingcat
 */
public class ShardingMultiTableSpace {
    private SchemaConfig schema;
    private static int total=1000000;
    protected LayerCachePool cachePool = new SimpleCachePool();
    public ShardingMultiTableSpace() throws InterruptedException {
         String schemaFile = "/route/schema.xml";
 		String ruleFile = "/route/rule.xml";
 		SchemaLoader schemaLoader = new XMLSchemaLoader(schemaFile, ruleFile);
 		schema = schemaLoader.getSchemas().get("cndb");
    }

    /**
     * 路由到tableSpace的性能测试
     * 
     * @throws SQLNonTransientException
     */
    public void testTableSpace() throws SQLNonTransientException {
        SchemaConfig schema = getSchema();
        String sql = "select id,member_id,gmt_create from offer where member_id in ('1','22','333','1124','4525')";
        for (int i = 0; i < total; i++) {
            RouteStrategyFactory.getRouteStrategy().route(new SystemConfig(),schema, -1,sql, null, null,cachePool);
        }
    }

    protected SchemaConfig getSchema() {
        return schema;
    }

    public static void main(String[] args) throws Exception {
        ShardingMultiTableSpace test = new ShardingMultiTableSpace();
        System.currentTimeMillis();

        long start = System.currentTimeMillis();
        test.testTableSpace();
        long end = System.currentTimeMillis();
        System.out.println("take " + (end - start) + " ms. avg "+(end-start+0.0)/total);
    }
}