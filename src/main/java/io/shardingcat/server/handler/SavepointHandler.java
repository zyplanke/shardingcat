
package io.shardingcat.server.handler;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.server.ServerConnection;

/**
 * @author shardingcat
 */
public final class SavepointHandler {

    public static void handle(String stmt, ServerConnection c) {
        c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
    }

}