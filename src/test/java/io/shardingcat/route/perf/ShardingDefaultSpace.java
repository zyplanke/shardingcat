
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
public class ShardingDefaultSpace {
    private SchemaConfig schema;
    private static int total=1000000;
    protected LayerCachePool cachePool = new SimpleCachePool();
    public ShardingDefaultSpace() throws InterruptedException {
         String schemaFile = "/route/schema.xml";
 		String ruleFile = "/route/rule.xml";
 		SchemaLoader schemaLoader = new XMLSchemaLoader(schemaFile, ruleFile);
 		schema = schemaLoader.getSchemas().get("cndb");
    }

    /**
     * 路由到defaultSpace的性能测试
     */
    public void testDefaultSpace() throws SQLNonTransientException {
        SchemaConfig schema = this.getSchema();
        String sql = "insert into offer (member_id, gmt_create) values ('1','2001-09-13 20:20:33')";
        for (int i = 0; i < total; i++) {
            RouteStrategyFactory.getRouteStrategy().route(new SystemConfig(),schema,-1, sql, null, null,cachePool);
        }
    }

    protected SchemaConfig getSchema() {
        return schema;
    }

    public static void main(String[] args) throws Exception {
        ShardingDefaultSpace test = new ShardingDefaultSpace();
        System.currentTimeMillis();

        long start = System.currentTimeMillis();
        test.testDefaultSpace();
        long end = System.currentTimeMillis();
        System.out.println("take " + (end - start) + " ms. avg "+(end-start+0.0)/total);
    }
}