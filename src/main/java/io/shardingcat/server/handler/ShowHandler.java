
package io.shardingcat.server.handler;

import io.shardingcat.server.ServerConnection;
import io.shardingcat.server.parser.ServerParse;
import io.shardingcat.server.parser.ServerParseShow;
import io.shardingcat.server.response.*;
import io.shardingcat.util.StringUtil;

/**
 * @author shardingcat
 */
public final class ShowHandler {

	public static void handle(String stmt, ServerConnection c, int offset) {

		// 排除 “ ` ” 符号
		stmt = StringUtil.replaceChars(stmt, "`", null);

		int type = ServerParseShow.parse(stmt, offset);
		switch (type) {
		case ServerParseShow.DATABASES:
			ShowDatabases.response(c);
			break;
		case ServerParseShow.TABLES:
			ShowTables.response(c, stmt,type);
			break;
            case ServerParseShow.FULLTABLES:
                ShowFullTables.response(c, stmt,type);
                break;
		case ServerParseShow.SHARDINGCAT_STATUS:
			ShowShardingCatStatus.response(c);
			break;
		case ServerParseShow.SHARDINGCAT_CLUSTER:
			ShowShardingCatCluster.response(c);
			break;
		default:
			c.execute(stmt, ServerParse.SHOW);
		}
	}

}