
package io.shardingcat.manager.response;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.config.Fields;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.util.IntegerUtil;
import io.shardingcat.util.LongUtil;
import io.shardingcat.util.NameableExecutor;
import io.shardingcat.util.StringUtil;

/**
 * 查看线程池状态
 * 
 * @author shardingcat
 * @author shardingcat
 */
public final class ShowThreadPool {

	private static final int FIELD_COUNT = 6;
	private static final ResultSetHeaderPacket header = PacketUtil
			.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();
	static {
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;

		fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("POOL_SIZE", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("ACTIVE_COUNT", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("TASK_QUEUE_SIZE",
				Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("COMPLETED_TASK",
				Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("TOTAL_TASK",
				Fields.FIELD_TYPE_LONGLONG);
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
		List<NameableExecutor> executors = getExecutors();
		for (NameableExecutor exec : executors) {
			if (exec != null) {
				RowDataPacket row = getRow(exec, c.getCharset());
				row.packetId = ++packetId;
				buffer = row.write(buffer, c, true);
			}
		}

		// write last eof
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c, true);

		// write buffer
		c.write(buffer);
	}

	private static RowDataPacket getRow(NameableExecutor exec, String charset) {
		RowDataPacket row = new RowDataPacket(FIELD_COUNT);
		row.add(StringUtil.encode(exec.getName(), charset));
		row.add(IntegerUtil.toBytes(exec.getPoolSize()));
		row.add(IntegerUtil.toBytes(exec.getActiveCount()));
		row.add(IntegerUtil.toBytes(exec.getQueue().size()));
		row.add(LongUtil.toBytes(exec.getCompletedTaskCount()));
		row.add(LongUtil.toBytes(exec.getTaskCount()));
		return row;
	}

	private static List<NameableExecutor> getExecutors() {
		List<NameableExecutor> list = new LinkedList<NameableExecutor>();
		ShardingCatServer server = ShardingCatServer.getInstance();
		list.add(server.getTimerExecutor());
		// list.add(server.getAioExecutor());
		list.add(server.getBusinessExecutor());
		// for (NIOProcessor pros : server.getProcessors()) {
		// list.add(pros.getExecutor());
		// }
		return list;
	}
}