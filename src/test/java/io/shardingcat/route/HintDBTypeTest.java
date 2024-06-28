package io.shardingcat.route;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.SimpleCachePool;
import io.shardingcat.cache.CacheService;
import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.loader.SchemaLoader;
import io.shardingcat.config.loader.xml.XMLSchemaLoader;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.route.factory.RouteStrategyFactory;
import io.shardingcat.server.parser.ServerParse;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class HintDBTypeTest {
	protected Map<String, SchemaConfig> schemaMap;
	protected LayerCachePool cachePool = new SimpleCachePool();
	protected RouteStrategy routeStrategy;

	public HintDBTypeTest() {
		String schemaFile = "/route/schema.xml";
		String ruleFile = "/route/rule.xml";
		SchemaLoader schemaLoader = new XMLSchemaLoader(schemaFile, ruleFile);
		schemaMap = schemaLoader.getSchemas();
		ShardingCatServer.getInstance().getConfig().getSchemas().putAll(schemaMap);
        RouteStrategyFactory.init();
        routeStrategy = RouteStrategyFactory.getRouteStrategy("druidparser");
	}
	/**
     * 测试注解
     *
     * @throws Exception
     */
    @Test
    public void testHint() throws Exception {
        SchemaConfig schema = schemaMap.get("TESTDB");
       //使用注解（新注解，/*!shardingcat*/），runOnSlave=false 强制走主节点
        String sql = "/*!shardingcat:db_type=master*/select * from employee where sharding_id=1";
        CacheService cacheService = new CacheService();
        RouteService routerService = new RouteService(cacheService);
        RouteResultset rrs = routerService.route(new SystemConfig(), schema, ServerParse.SELECT, sql, "UTF-8", null);
        Assert.assertTrue(!rrs.getRunOnSlave());

        //使用注解（新注解，/*#shardingcat*/），runOnSlave=false 强制走主节点
        sql = "/*#shardingcat:db_type=master*/select * from employee where sharding_id=1";
        rrs = routerService.route(new SystemConfig(), schema, ServerParse.SELECT, sql, "UTF-8", null);
        Assert.assertTrue(!rrs.getRunOnSlave());
        
        //使用注解（新注解，/*shardingcat*/），runOnSlave=false 强制走主节点
        sql = "/*shardingcat:db_type=master*/select * from employee where sharding_id=1";
        rrs = routerService.route(new SystemConfig(), schema, ServerParse.SELECT, sql, "UTF-8", null);
        Assert.assertTrue(!rrs.getRunOnSlave());
        
        //不使用注解，runOnSlave=null, 根据读写分离策略走主从库
        sql = "select * from employee where sharding_id=1";
        rrs = routerService.route(new SystemConfig(), schema, ServerParse.SELECT, sql, "UTF-8", null);
        Assert.assertTrue(rrs.getRunOnSlave()==null);
    }
}
