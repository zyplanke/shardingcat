
package io.shardingcat.server.handler;

import io.shardingcat.server.ServerConnection;

/**
 * @author shardingcat
 */
public final class BeginHandler {
    private static final byte[] AC_OFF = new byte[] { 7, 0, 0, 1, 0, 0, 0, 0,
            0, 0, 0 };
    public static void handle(String stmt, ServerConnection c) {
        if (c.isAutocommit())
        {
            c.write(c.writeToBuffer(AC_OFF, c.allocate()));
        }else
        {
            c.getSession2().commit() ;
        }
        c.setAutocommit(false);
    }

}