package io.shardingcat.manager.response;

import java.nio.ByteBuffer;
import java.util.Collection;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.BackendConnection;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.config.Fields;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.FrontendConnection;
import io.shardingcat.net.NIOProcessor;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.server.NonBlockingSession;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.util.StringUtil;

/**
 * show front session detail info
 * 
 * @author wuzhih
 * 
 */
public class ShowSession {
	private static final int FIELD_COUNT = 3;
	private static final ResultSetHeaderPacket header = PacketUtil
			.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();
	static {
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;

		fields[i] = PacketUtil.getField("SESSION", Fields.FIELD_TYPE_VARCHAR);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("DN_COUNT", Fields.FIELD_TYPE_VARCHAR);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("DN_LIST", Fields.FIELD_TYPE_VARCHAR);
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
		for (NIOProcessor process : ShardingCatServer.getInstance().getProcessors()) {
			for (FrontendConnection front : process.getFrontends().values()) {

				if (!(front instanceof ServerConnection)) {
					continue;
				}
				ServerConnection sc = (ServerConnection) front;
				RowDataPacket row = getRow(sc, c.getCharset());
				if (row != null) {
					row.packetId = ++packetId;
					buffer = row.write(buffer, c, true);
				}
			}
		}

		// write last eof
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c, true);

		// write buffer
		c.write(buffer);
	}

	private static RowDataPacket getRow(ServerConnection sc, String charset) {
		StringBuilder sb = new StringBuilder();
		NonBlockingSession ssesion = sc.getSession2();
		Collection<BackendConnection> backConnections = ssesion.getTargetMap()
				.values();
		int cncount = backConnections.size();
		if (cncount == 0) {
			return null;
		}
		for (BackendConnection backCon : backConnections) {
			sb.append(backCon).append("\r\n");
		}
		RowDataPacket row = new RowDataPacket(FIELD_COUNT);
		row.add(StringUtil.encode(sc.getId() + "", charset));
		row.add(StringUtil.encode(cncount + "", charset));
		row.add(StringUtil.encode(sb.toString(), charset));
		return row;
	}
}
