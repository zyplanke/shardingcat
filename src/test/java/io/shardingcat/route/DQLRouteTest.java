package io.shardingcat.route;

import java.lang.reflect.Method;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.SimpleCachePool;
import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.loader.SchemaLoader;
import io.shardingcat.config.loader.xml.XMLSchemaLoader;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.route.parser.druid.DruidShardingParseInfo;
import io.shardingcat.route.parser.druid.ShardingCatSchemaStatVisitor;
import io.shardingcat.route.parser.druid.ShardingCatStatementParser;
import io.shardingcat.route.parser.druid.RouteCalculateUnit;
import io.shardingcat.route.factory.RouteStrategyFactory;


import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat.Condition;

import junit.framework.Assert;

public class DQLRouteTest {

	protected Map<String, SchemaConfig> schemaMap;
	protected LayerCachePool cachePool = new SimpleCachePool();
	protected RouteStrategy routeStrategy = RouteStrategyFactory.getRouteStrategy("druidparser");
	private Map<String, String> tableAliasMap = new HashMap<String, String>();

	protected DruidShardingParseInfo ctx;

	public DQLRouteTest() {
		String schemaFile = "/route/schema.xml";
		String ruleFile = "/route/rule.xml";
		SchemaLoader schemaLoader = new XMLSchemaLoader(schemaFile, ruleFile);
		schemaMap = schemaLoader.getSchemas();
		ShardingCatServer.getInstance().getConfig().getSchemas().putAll(schemaMap);
	}

	@Test
	public void test() throws Exception {
		String stmt = "select * from `offer` where id = 100";
		SchemaConfig schema = schemaMap.get("mysqldb");
		RouteResultset rrs = new RouteResultset(stmt, 7);
		SQLStatementParser parser = null;
		if (schema.isNeedSupportMultiDBType()) {
			parser = new ShardingCatStatementParser(stmt);
		} else {
			parser = new MySqlStatementParser(stmt);
		}
		SQLStatement statement;
		ShardingCatSchemaStatVisitor visitor = null;

		try {
			statement = parser.parseStatement();
			visitor = new ShardingCatSchemaStatVisitor();
		} catch (Exception t) {
			throw new SQLSyntaxErrorException(t);
		}
		ctx = new DruidShardingParseInfo();
		ctx.setSql(stmt);

		List<RouteCalculateUnit> taskList = visitorParse(rrs, statement, visitor);
		Assert.assertEquals(true, !taskList.get(0).getTablesAndConditions().isEmpty());
	}

	@SuppressWarnings("unchecked")
	private List<RouteCalculateUnit> visitorParse(RouteResultset rrs, SQLStatement stmt, ShardingCatSchemaStatVisitor visitor) throws Exception {

		stmt.accept(visitor);

		List<List<Condition>> mergedConditionList = new ArrayList<List<Condition>>();
		if (visitor.hasOrCondition()) {// 包含or语句
			// TODO
			// 根据or拆分
			mergedConditionList = visitor.splitConditions();
		} else {// 不包含OR语句
			mergedConditionList.add(visitor.getConditions());
		}

		if (visitor.getAliasMap() != null) {
			for (Map.Entry<String, String> entry : visitor.getAliasMap().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (key != null && key.indexOf("`") >= 0) {
					key = key.replaceAll("`", "");
				}
				if (value != null && value.indexOf("`") >= 0) {
					value = value.replaceAll("`", "");
				}
				// 表名前面带database的，去掉
				if (key != null) {
					int pos = key.indexOf(".");
					if (pos > 0) {
						key = key.substring(pos + 1);
					}
				}

				if (key.equals(value)) {
					ctx.addTable(key.toUpperCase());
				}
				// else {
				// tableAliasMap.put(key, value);
				// }
				tableAliasMap.put(key.toUpperCase(), value);
			}
			visitor.getAliasMap().putAll(tableAliasMap);
			ctx.setTableAliasMap(tableAliasMap);
		}

		//利用反射机制单元测试DefaultDruidParser类的私有方法buildRouteCalculateUnits
		Class<?> clazz = Class.forName("io.shardingcat.route.parser.druid.impl.DefaultDruidParser");
		Method buildRouteCalculateUnits = clazz.getDeclaredMethod("buildRouteCalculateUnits",
				new Class[] { SchemaStatVisitor.class, List.class });
		//System.out.println("buildRouteCalculateUnits:\t" + buildRouteCalculateUnits);
		Object newInstance = clazz.newInstance();
		buildRouteCalculateUnits.setAccessible(true);
		Object returnValue = buildRouteCalculateUnits.invoke(newInstance,
				new Object[] { visitor, mergedConditionList });
		List<RouteCalculateUnit> retList = new ArrayList<RouteCalculateUnit>();
		if (returnValue instanceof ArrayList<?>) {
			retList.add(((ArrayList<RouteCalculateUnit>)returnValue).get(0));
			//retList = (ArrayList<RouteCalculateUnit>)returnValue;
			//System.out.println(taskList.get(0).getTablesAndConditions().values());			
		}
		return retList;
	}

}
