package io.shardingcat.route;

import java.sql.SQLNonTransientException;
import java.util.Map;

import org.junit.Test;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.SimpleCachePool;
import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.loader.SchemaLoader;
import io.shardingcat.config.loader.xml.XMLSchemaLoader;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.route.factory.RouteStrategyFactory;
import junit.framework.Assert;

public class DruidDb2SqlParserTest
{
	protected Map<String, SchemaConfig> schemaMap;
	protected LayerCachePool cachePool = new SimpleCachePool();
    protected RouteStrategy routeStrategy;

	public DruidDb2SqlParserTest() {
		String schemaFile = "/route/schema.xml";
		String ruleFile = "/route/rule.xml";
		SchemaLoader schemaLoader = new XMLSchemaLoader(schemaFile, ruleFile);
		schemaMap = schemaLoader.getSchemas();
		ShardingCatServer.getInstance().getConfig().getSchemas().putAll(schemaMap);
        RouteStrategyFactory.init();
        routeStrategy = RouteStrategyFactory.getRouteStrategy("druidparser");
	}

	@Test
	public void testLimitToDb2Page() throws SQLNonTransientException {
		String sql = "select * from offer order by id desc limit 5,10";
		SchemaConfig schema = schemaMap.get("db2db");
		RouteResultset rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
	                null, cachePool);        
        Assert.assertEquals(2, rrs.getNodes().length);
        Assert.assertEquals(5, rrs.getLimitStart());
        Assert.assertEquals(10, rrs.getLimitSize());
        Assert.assertEquals(0, rrs.getNodes()[0].getLimitStart());
        Assert.assertEquals(15, rrs.getNodes()[0].getLimitSize());
        Assert.assertEquals("db2_1", rrs.getNodes()[0].getName());
        Assert.assertEquals("db2_2", rrs.getNodes()[1].getName());

        sql= rrs.getNodes()[0].getStatement() ;
        rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
                null, cachePool);
        Assert.assertEquals(0, rrs.getNodes()[0].getLimitStart());
        Assert.assertEquals(15, rrs.getNodes()[0].getLimitSize());
        Assert.assertEquals(0, rrs.getLimitStart());
        Assert.assertEquals(15, rrs.getLimitSize());
		
        sql="select * from offer1 order by id desc limit 5,10" ;
        rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
                null, cachePool);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(5, rrs.getLimitStart());
        Assert.assertEquals(10, rrs.getLimitSize());
        Assert.assertEquals(5, rrs.getNodes()[0].getLimitStart());
        Assert.assertEquals(10, rrs.getNodes()[0].getLimitSize());
        Assert.assertEquals("db2_1", rrs.getNodes()[0].getName());

	}



    @Test
    public void testDb2PageSQL() throws SQLNonTransientException {
        String sql = "SELECT *\n" +
                "FROM (SELECT sid, ROW_NUMBER() OVER (ORDER BY sid DESC) AS ROWNUM\n" +
                "\tFROM offer \n" +
                "\tWHERE sts <> 'N'\n" +
                "\t\t\t) XX\n" +
                "WHERE ROWNUM > 5\n" +
                "\tAND ROWNUM <= 15\n";
        SchemaConfig schema = schemaMap.get("db2db");
        RouteResultset rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
                null, cachePool);
        Assert.assertEquals(2, rrs.getNodes().length);
        Assert.assertEquals(5, rrs.getLimitStart());
        Assert.assertEquals(10, rrs.getLimitSize());
        Assert.assertEquals(0, rrs.getNodes()[0].getLimitStart());
        Assert.assertEquals(15, rrs.getNodes()[0].getLimitSize());
        Assert.assertEquals("db2_1", rrs.getNodes()[0].getName());
        Assert.assertEquals("db2_2", rrs.getNodes()[1].getName());

        sql = "SELECT *\n" +
                "FROM (SELECT sid, ROW_NUMBER() OVER (ORDER BY sid DESC) AS ROWNUM\n" +
                "\tFROM offer1 \n" +
                "\tWHERE sts <> 'N'\n" +
                "\t\t\t) XX\n" +
                "WHERE ROWNUM > 5\n" +
                "\tAND ROWNUM <= 15\n";
        rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
                null, cachePool);
        Assert.assertEquals(1, rrs.getNodes().length);
        Assert.assertEquals(5, rrs.getLimitStart());
        Assert.assertEquals(10, rrs.getLimitSize());
        Assert.assertEquals(5, rrs.getNodes()[0].getLimitStart());
        Assert.assertEquals(10, rrs.getNodes()[0].getLimitSize());
        Assert.assertEquals(sql,rrs.getNodes()[0].getStatement()) ;
        Assert.assertEquals("db2_1", rrs.getNodes()[0].getName());






        sql="SELECT sid\n" +
                "FROM offer  \n" +
                "ORDER BY sid desc\n" +
                "FETCH FIRST 10  ROWS ONLY"  ;
        rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
                null, cachePool);
        Assert.assertEquals(2, rrs.getNodes().length);
        Assert.assertEquals(0, rrs.getLimitStart());
        Assert.assertEquals(10, rrs.getLimitSize());
        Assert.assertEquals(0, rrs.getNodes()[0].getLimitStart());
        Assert.assertEquals(10, rrs.getNodes()[0].getLimitSize());
        Assert.assertEquals(sql,rrs.getNodes()[0].getStatement()) ;
        Assert.assertEquals("db2_1", rrs.getNodes()[0].getName());
        Assert.assertEquals("db2_2", rrs.getNodes()[1].getName());

    }
}
