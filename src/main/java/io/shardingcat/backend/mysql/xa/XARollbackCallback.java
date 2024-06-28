package io.shardingcat.backend.mysql.xa;

import io.shardingcat.sqlengine.SQLQueryResult;
import io.shardingcat.sqlengine.SQLQueryResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by zhangchao on 2016/10/18.
 */
public class XARollbackCallback implements SQLQueryResultListener<SQLQueryResult<Map<String, String>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XARollbackCallback.class);

    public void onResult(SQLQueryResult<Map<String, String>> result) {

        LOGGER.debug("[CALLBACK][XA ROLLBACK] when ShardingCat start");


    }
}
