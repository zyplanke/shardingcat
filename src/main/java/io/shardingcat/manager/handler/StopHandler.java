
package io.shardingcat.manager.handler;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.manager.response.StopHeartbeat;
import io.shardingcat.route.parser.ManagerParseStop;

/**
 * @author shardingcat
 */
public final class StopHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseStop.parse(stmt, offset)) {
        case ManagerParseStop.HEARTBEAT:
            StopHeartbeat.execute(stmt, c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}