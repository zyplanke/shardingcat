
package io.shardingcat.parser;

import io.shardingcat.route.parser.ManagerParse;

/**
 * @author shardingcat
 */
public class ManagerParserTestPerf {

    public void testPerformance() {
        for (int i = 0; i < 250000; i++) {
            ManagerParse.parse("show databases");
            ManagerParse.parse("set autocommit=1");
            ManagerParse.parse(" show  @@datasource ");
            ManagerParse.parse("select id,name,value from t");
        }
    }

    public void testPerformanceWhere() {
        for (int i = 0; i < 500000; i++) {
            ManagerParse.parse(" show  @@datasource where datanode = 1");
            ManagerParse.parse(" show  @@datanode where schema = 1");
        }
    }

}