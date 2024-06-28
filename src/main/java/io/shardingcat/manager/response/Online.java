
package io.shardingcat.manager.response;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.OkPacket;

/**
 * @author shardingcat
 */
public class Online {

    private static final OkPacket ok = new OkPacket();
    static {
        ok.packetId = 1;
        ok.affectedRows = 1;
        ok.serverStatus = 2;
    }

    public static void execute(String stmt, ManagerConnection mc) {
        ShardingCatServer.getInstance().online();
        ok.write(mc);
    }

}