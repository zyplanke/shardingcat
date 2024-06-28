
package io.shardingcat.server.response;

import java.nio.ByteBuffer;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.config.Fields;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.ErrorPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.util.RandomUtil;
import io.shardingcat.util.StringUtil;

/**
 * @author shardingcat
 */
public class SelectConnnectID {

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    private static final ErrorPacket error = PacketUtil.getShutdown();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("CONNECTION_ID()", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(ServerConnection c) {
        if (ShardingCatServer.getInstance().isOnline()) {
            ByteBuffer buffer = c.allocate();
            buffer = header.write(buffer, c,true);
            for (FieldPacket field : fields) {
                buffer = field.write(buffer, c,true);
            }
            buffer = eof.write(buffer, c,true);
            byte packetId = eof.packetId;
            RowDataPacket row = new RowDataPacket(FIELD_COUNT);
            row.add(getConnectID(c));
            row.packetId = ++packetId;
            buffer = row.write(buffer, c,true);
            EOFPacket lastEof = new EOFPacket();
            lastEof.packetId = ++packetId;
            buffer = lastEof.write(buffer, c,true);
            c.write(buffer);
        } else {
            error.write(c);
        }
    }

    private static byte[] getConnectID(ServerConnection c) {
        StringBuilder sb = new StringBuilder();
        sb.append(new String(RandomUtil.randomBytes(10000)));
        return StringUtil.encode(sb.toString(), c.getCharset());
    }

}