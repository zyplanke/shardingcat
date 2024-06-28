
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
public class NoShardingSpace {
    private SchemaConfig schema;
    private static int total=1000000;
    protected LayerCachePool cachePool = new SimpleCachePool();
    public NoShardingSpace() {
    	String schemaFile = "/route/schema.xml";
		String ruleFile = "/route/rule.xml";
		SchemaLoader schemaLoader = new XMLSchemaLoader(schemaFile, ruleFile);
		schema = schemaLoader.getSchemas().get("dubbo");
    }

    public void testDefaultSpace() throws SQLNonTransientException {
        SchemaConfig schema = this.schema;
        String stmt = "insert into offer (member_id, gmt_create) values ('1','2001-09-13 20:20:33')";
        for (int i = 0; i < total; i++) {
            RouteStrategyFactory.getRouteStrategy().route(new SystemConfig(),schema, -1,stmt, null, null,cachePool);
        }
    }

    public static void main(String[] args) throws SQLNonTransientException {
        NoShardingSpace test = new NoShardingSpace();
        System.currentTimeMillis();

        long start = System.currentTimeMillis();
        test.testDefaultSpace();
        long end = System.currentTimeMillis();
        System.out.println("take " + (end - start) + " ms. avg "+(end-start+0.0)/total);
    }
}