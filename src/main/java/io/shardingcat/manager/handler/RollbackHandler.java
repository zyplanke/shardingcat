
package io.shardingcat.manager.handler;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.manager.response.RollbackConfig;
import io.shardingcat.manager.response.RollbackUser;
import io.shardingcat.route.parser.ManagerParseRollback;

/**
 * @author shardingcat
 */
public final class RollbackHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseRollback.parse(stmt, offset)) {
        case ManagerParseRollback.CONFIG:
            RollbackConfig.execute(c);
            break;
        case ManagerParseRollback.ROUTE:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            break;
        case ManagerParseRollback.USER:
            RollbackUser.execute(c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}