package io.shardingcat.route.parser.druid.impl;

import java.sql.SQLNonTransientException;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;

import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.TableConfig;
import io.shardingcat.route.RouteResultset;
import io.shardingcat.route.function.AbstractPartitionAlgorithm;
import io.shardingcat.route.function.SlotFunction;
import io.shardingcat.route.parser.druid.ShardingCatSchemaStatVisitor;
import io.shardingcat.util.StringUtil;


public class DruidCreateTableParser extends DefaultDruidParser {

	@Override
	public void visitorParse(RouteResultset rrs, SQLStatement stmt, ShardingCatSchemaStatVisitor visitor) {
	}
	
	@Override
	public void statementParse(SchemaConfig schema, RouteResultset rrs, SQLStatement stmt) throws SQLNonTransientException {
		MySqlCreateTableStatement createStmt = (MySqlCreateTableStatement)stmt;
		if(createStmt.getQuery() != null) {
			String msg = "create table from other table not supported :" + stmt;
			LOGGER.warn(msg);
			throw new SQLNonTransientException(msg);
		}
		String tableName = StringUtil.removeBackquote(createStmt.getTableSource().toString().toUpperCase());
		if(schema.getTables().containsKey(tableName)) {
			TableConfig tableConfig = schema.getTables().get(tableName);
			AbstractPartitionAlgorithm algorithm = tableConfig.getRule().getRuleAlgorithm();
			if(algorithm instanceof SlotFunction){
				SQLColumnDefinition column = new SQLColumnDefinition();
				column.setDataType(new SQLCharacterDataType("int"));
				column.setName(new SQLIdentifierExpr("_slot"));
				column.setComment(new SQLCharExpr("自动迁移算法slot,禁止修改"));
				((SQLCreateTableStatement)stmt).getTableElementList().add(column);
				String sql = createStmt.toString();
				rrs.setStatement(sql);
				ctx.setSql(sql);
			}
		}
		ctx.addTable(tableName);
		
	}
}
