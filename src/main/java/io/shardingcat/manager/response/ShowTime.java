
package io.shardingcat.manager.response;

import java.nio.ByteBuffer;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.config.Fields;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.route.parser.ManagerParseShow;
import io.shardingcat.util.LongUtil;

/**
 * @author shardingcat
 */
public final class ShowTime {

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("TIMESTAMP", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void execute(ManagerConnection c, int type) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c,true);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c,true);
        }

        // write eof
        buffer = eof.write(buffer, c,true);

        // write rows
        byte packetId = eof.packetId;
        RowDataPacket row = getRow(type);
        row.packetId = ++packetId;
        buffer = row.write(buffer, c,true);

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c,true);

        // post write
        c.write(buffer);
    }

    private static RowDataPacket getRow(int type) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        switch (type) {
        case ManagerParseShow.TIME_CURRENT:
            row.add(LongUtil.toBytes(System.currentTimeMillis()));
            break;
        case ManagerParseShow.TIME_STARTUP:
            row.add(LongUtil.toBytes(ShardingCatServer.getInstance().getStartupTime()));
            break;
        default:
            row.add(LongUtil.toBytes(0L));
        }
        return row;
    }

}