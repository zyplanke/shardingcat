
package io.shardingcat.server.handler;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.config.ErrorCode;
import io.shardingcat.net.FrontendConnection;
import io.shardingcat.net.NIOProcessor;
import io.shardingcat.net.mysql.OkPacket;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.util.StringUtil;

/**
 * @author shardingcat
 */
public class KillHandler {

    public static void handle(String stmt, int offset, ServerConnection c) {
        String id = stmt.substring(offset).trim();
        if (StringUtil.isEmpty(id)) {
            c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "NULL connection id");
        } else {
            // get value
            long value = 0;
            try {
                value = Long.parseLong(id);
            } catch (NumberFormatException e) {
                c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "Invalid connection id:" + id);
                return;
            }

            // kill myself
            if (value == c.getId()) {
                getOkPacket().write(c);
                c.write(c.allocate());
                return;
            }

            // get connection and close it
            FrontendConnection fc = null;
            NIOProcessor[] processors = ShardingCatServer.getInstance().getProcessors();
            for (NIOProcessor p : processors) {
                if ((fc = p.getFrontends().get(value)) != null) {
                    break;
                }
            }
            if (fc != null) {
                fc.close("killed");
                getOkPacket().write(c);
            } else {
                c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "Unknown connection id:" + id);
            }
        }
    }

    private static OkPacket getOkPacket() {
        OkPacket packet = new OkPacket();
        packet.packetId = 1;
        packet.affectedRows = 0;
        packet.serverStatus = 2;
        return packet;
    }

}