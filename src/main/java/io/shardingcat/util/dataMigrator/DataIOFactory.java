package io.shardingcat.util.dataMigrator;

import io.shardingcat.util.dataMigrator.dataIOImpl.MysqlDataIO;
import io.shardingcat.util.exception.DataMigratorException;

public class DataIOFactory {

	public static final String MYSQL = "mysql";
	public static final String ORACLE = "oracle";
	
	public static DataIO createDataIO(String dbType){
		switch (dbType) {
		case MYSQL:
			return new MysqlDataIO();
		default:
			throw new DataMigratorException("dbType:"+dbType+" is not support for the moment!");
		}
	}
}
