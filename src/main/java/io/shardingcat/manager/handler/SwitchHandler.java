
package io.shardingcat.manager.handler;

import static io.shardingcat.route.parser.ManagerParseSwitch.DATASOURCE;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.manager.response.SwitchDataSource;
import io.shardingcat.route.parser.ManagerParseSwitch;

/**
 * @author shardingcat
 */
public final class SwitchHandler {

    public static void handler(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseSwitch.parse(stmt, offset)) {
        case DATASOURCE:
            SwitchDataSource.response(stmt, c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}