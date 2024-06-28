
package io.shardingcat.server.response;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.net.FrontendConnection;
import io.shardingcat.net.mysql.ErrorPacket;
import io.shardingcat.net.mysql.OkPacket;

/**
 * 加入了offline状态推送，用于心跳语句。
 * 
 * @author shardingcat
 */
public class Ping {

    private static final ErrorPacket error = PacketUtil.getShutdown();

    public static void response(FrontendConnection c) {
        if (ShardingCatServer.getInstance().isOnline()) {
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            error.write(c);
        }
    }

}