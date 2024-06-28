
package io.shardingcat.server.response;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.config.Alarms;
import io.shardingcat.config.Fields;
import io.shardingcat.config.ShardingCatCluster;
import io.shardingcat.config.ShardingCatConfig;
import io.shardingcat.config.ShardingCatNode;
import io.shardingcat.config.model.ShardingCatNodeConfig;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.util.IntegerUtil;
import io.shardingcat.util.StringUtil;

/**
 * @author shardingcat
 */
public class ShowCobarCluster {

    private static final Logger alarm = LoggerFactory.getLogger("alarm");

    private static final int FIELD_COUNT = 2;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("WEIGHT", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(ServerConnection c) {
        ByteBuffer buffer = c.allocate();

        // write header
        buffer = header.write(buffer, c,true);

        // write field
        for (FieldPacket field : fields) {
            buffer = field.write(buffer, c,true);
        }

        // write eof
        buffer = eof.write(buffer, c,true);

        // write rows
        byte packetId = eof.packetId;
        for (RowDataPacket row : getRows(c)) {
            row.packetId = ++packetId;
            buffer = row.write(buffer, c,true);
        }

        // last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c,true);

        // post write
        c.write(buffer);
    }

    private static List<RowDataPacket> getRows(ServerConnection c) {
        List<RowDataPacket> rows = new LinkedList<RowDataPacket>();
        ShardingCatConfig config = ShardingCatServer.getInstance().getConfig();
        ShardingCatCluster cluster = config.getCluster();
        Map<String, SchemaConfig> schemas = config.getSchemas();
        SchemaConfig schema = (c.getSchema() == null) ? null : schemas.get(c.getSchema());

        // 如果没有指定schema或者schema为null，则使用全部集群。
        if (schema == null) {
            Map<String, ShardingCatNode> nodes = cluster.getNodes();
            for (ShardingCatNode n : nodes.values()) {
                if (n != null && n.isOnline()) {
                    rows.add(getRow(n, c.getCharset()));
                }
            }
        } else {

        	 Map<String, ShardingCatNode> nodes = cluster.getNodes();
             for (ShardingCatNode n : nodes.values()) {
                 if (n != null && n.isOnline()) {
                     rows.add(getRow(n, c.getCharset()));
                 }
             }
        }

        if (rows.size() == 0) {
            alarm.error(Alarms.CLUSTER_EMPTY + c.toString());
        }

        return rows;
    }

    private static RowDataPacket getRow(ShardingCatNode node, String charset) {
        ShardingCatNodeConfig conf = node.getConfig();
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(conf.getHost(), charset));
        row.add(IntegerUtil.toBytes(conf.getWeight()));
        return row;
    }

}