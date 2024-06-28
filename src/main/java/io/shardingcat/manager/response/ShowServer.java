
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
import io.shardingcat.util.FormatUtil;
import io.shardingcat.util.LongUtil;
import io.shardingcat.util.StringUtil;
import io.shardingcat.util.TimeUtil;

/**
 * 服务器状态报告
 * 
 * @author shardingcat
 * @author shardingcat
 */
public final class ShowServer {

	private static final int FIELD_COUNT = 8;
	private static final ResultSetHeaderPacket header = PacketUtil
			.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();
	static {
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;

		fields[i] = PacketUtil.getField("UPTIME", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("USED_MEMORY",
				Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("TOTAL_MEMORY",
				Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("MAX_MEMORY",
				Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("RELOAD_TIME",
				Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("ROLLBACK_TIME",
				Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil
				.getField("CHARSET", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("STATUS", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		eof.packetId = ++packetId;
	}

	public static void execute(ManagerConnection c) {
		ByteBuffer buffer = c.allocate();

		// write header
		buffer = header.write(buffer, c, true);

		// write fields
		for (FieldPacket field : fields) {
			buffer = field.write(buffer, c, true);
		}

		// write eof
		buffer = eof.write(buffer, c, true);

		// write rows
		byte packetId = eof.packetId;
		RowDataPacket row = getRow(c.getCharset());
		row.packetId = ++packetId;
		buffer = row.write(buffer, c, true);

		// write last eof
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c, true);

		// write buffer
		c.write(buffer);
	}

	private static RowDataPacket getRow(String charset) {
		ShardingCatServer server = ShardingCatServer.getInstance();
		long startupTime = server.getStartupTime();
		long now = TimeUtil.currentTimeMillis();
		long uptime = now - startupTime;
		Runtime rt = Runtime.getRuntime();
		long total = rt.totalMemory();
		long max = rt.maxMemory();
		long used = (total - rt.freeMemory());
		RowDataPacket row = new RowDataPacket(FIELD_COUNT);
		row.add(StringUtil.encode(FormatUtil.formatTime(uptime, 3), charset));
		row.add(LongUtil.toBytes(used));
		row.add(LongUtil.toBytes(total));
		row.add(LongUtil.toBytes(max));
		row.add(LongUtil.toBytes(server.getConfig().getReloadTime()));
		row.add(LongUtil.toBytes(server.getConfig().getRollbackTime()));
		row.add(StringUtil.encode(charset, charset));
		row.add(StringUtil.encode(ShardingCatServer.getInstance().isOnline() ? "ON"
				: "OFF", charset));
		return row;
	}

}