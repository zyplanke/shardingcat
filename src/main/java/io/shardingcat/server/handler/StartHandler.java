
package io.shardingcat.server.handler;

import io.shardingcat.server.ServerConnection;
import io.shardingcat.server.parser.ServerParse;
import io.shardingcat.server.parser.ServerParseStart;

/**
 * @author shardingcat
 */
public final class StartHandler {
    private static final byte[] AC_OFF = new byte[] { 7, 0, 0, 1, 0, 0, 0, 0,
            0, 0, 0 };
    public static void handle(String stmt, ServerConnection c, int offset) {
        switch (ServerParseStart.parse(stmt, offset)) {
        case ServerParseStart.TRANSACTION:
            if (c.isAutocommit())
            {
                c.write(c.writeToBuffer(AC_OFF, c.allocate()));
            }else
            {
                c.getSession2().commit() ;
            }
            c.setAutocommit(false);
            break;
        default:
            c.execute(stmt, ServerParse.START);
        }
    }

}