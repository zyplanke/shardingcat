
package io.shardingcat.route.function;

import junit.framework.Assert;
import org.junit.Test;

import io.shardingcat.SimpleCachePool;
import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.loader.SchemaLoader;
import io.shardingcat.config.loader.xml.XMLSchemaLoader;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.route.RouteResultset;
import io.shardingcat.route.RouteStrategy;
import io.shardingcat.route.factory.RouteStrategyFactory;

import java.math.BigInteger;
import java.sql.SQLNonTransientException;
import java.util.Map;

public class PartitionByRangeModTest
{

    @Test
    public void test()  {
        PartitionByRangeMod autoPartition = new PartitionByRangeMod();
        autoPartition.setMapFile("partition-range-mod.txt");
        autoPartition.init();
        String idVal = "0";
        Assert.assertEquals(true, 0 == autoPartition.calculate(idVal));
        idVal = "1";
        Assert.assertEquals(true, 1 == autoPartition.calculate(idVal));
        idVal = "2";
        Assert.assertEquals(true, 2 == autoPartition.calculate(idVal));
        idVal = "3";
        Assert.assertEquals(true, 3 == autoPartition.calculate(idVal));
        idVal = "4";
        Assert.assertEquals(true, 4 == autoPartition.calculate(idVal));
        idVal = "5";
        Assert.assertEquals(true, 0 == autoPartition.calculate(idVal));

        idVal="2000000";
		Assert.assertEquals(true, 0==autoPartition.calculate(idVal));

		idVal="2000001";
		Assert.assertEquals(true, 5==autoPartition.calculate(idVal));

		idVal="4000000";
		Assert.assertEquals(true, 5==autoPartition.calculate(idVal));

		idVal="4000001";
		Assert.assertEquals(true, 7==autoPartition.calculate(idVal));
    }


    private static int mod(long v, int size)
    {
        BigInteger bigNum = BigInteger.valueOf(v).abs();
        return (bigNum.mod(BigInteger.valueOf(size))).intValue();
    }

    protected Map<String, SchemaConfig> schemaMap;
    protected LayerCachePool cachePool = new SimpleCachePool();
    protected RouteStrategy routeStrategy = RouteStrategyFactory.getRouteStrategy("druidparser");

    public PartitionByRangeModTest() {
        String schemaFile = "/route/schema.xml";
        String ruleFile = "/route/rule.xml";
        SchemaLoader schemaLoader = new XMLSchemaLoader(schemaFile, ruleFile);
        schemaMap = schemaLoader.getSchemas();
    }

    @Test
    public void testRange() throws SQLNonTransientException {
        String sql = "select * from offer  where id between 2000000  and 4000001     order by id desc limit 100";
        SchemaConfig schema = schemaMap.get("TESTDB");
        RouteResultset rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
                null, cachePool);
        Assert.assertEquals(10, rrs.getNodes().length);

        sql = "select * from offer  where id between 9  and 2000     order by id desc limit 100";
        rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
                null, cachePool);
        Assert.assertEquals(5, rrs.getNodes().length);

        sql = "select * from offer  where id between 4000001  and 6005001     order by id desc limit 100";
        rrs = routeStrategy.route(new SystemConfig(), schema, -1, sql, null,
                null, cachePool);
        Assert.assertEquals(8, rrs.getNodes().length);


    }
}