
package io.shardingcat.manager.response;

import java.nio.ByteBuffer;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.BackendConnection;
import io.shardingcat.backend.jdbc.JDBCConnection;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.backend.mysql.nio.MySQLConnection;
import io.shardingcat.config.Fields;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.BackendAIOConnection;
import io.shardingcat.net.NIOProcessor;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.util.IntegerUtil;
import io.shardingcat.util.LongUtil;
import io.shardingcat.util.StringUtil;
import io.shardingcat.util.TimeUtil;

/**
 * 查询后端连接
 * 
 * @author shardingcat
 */
public class ShowBackend {

	private static final int FIELD_COUNT = 16;
	private static final ResultSetHeaderPacket header = PacketUtil
			.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();
	static {
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;
		fields[i] = PacketUtil.getField("processor",
				Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("id", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("mysqlId", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("host", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("port", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("l_port", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("net_in", Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("net_out", Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("life", Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("closed", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		// fields[i] = PacketUtil.getField("run", Fields.FIELD_TYPE_VAR_STRING);
		// fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("borrowed",
				Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("SEND_QUEUE", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("schema", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil
				.getField("charset", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil
				.getField("txlevel", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("autocommit",
				Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		eof.packetId = ++packetId;
	}

	public static void execute(ManagerConnection c) {
		ByteBuffer buffer = c.allocate();
		buffer = header.write(buffer, c, true);
		for (FieldPacket field : fields) {
			buffer = field.write(buffer, c, true);
		}
		buffer = eof.write(buffer, c, true);
		byte packetId = eof.packetId;
		String charset = c.getCharset();
		for (NIOProcessor p : ShardingCatServer.getInstance().getProcessors()) {
			for (BackendConnection bc : p.getBackends().values()) {
				if (bc != null) {
					RowDataPacket row = getRow(bc, charset);
					row.packetId = ++packetId;
					buffer = row.write(buffer, c, true);
				}
			}
		}
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c, true);
		c.write(buffer);
	}

	private static RowDataPacket getRow(BackendConnection c, String charset) {
		RowDataPacket row = new RowDataPacket(FIELD_COUNT);
		if (c instanceof BackendAIOConnection) {
			row.add(((BackendAIOConnection) c).getProcessor().getName()
					.getBytes());
		} else if(c instanceof JDBCConnection){
		    row.add(((JDBCConnection)c).getProcessor().getName().getBytes());
		}else{
		    row.add("N/A".getBytes());
		}
		row.add(LongUtil.toBytes(c.getId()));
		long threadId = 0;
		if (c instanceof MySQLConnection) {
			threadId = ((MySQLConnection) c).getThreadId();
		}
		row.add(LongUtil.toBytes(threadId));
		row.add(StringUtil.encode(c.getHost(), charset));
		row.add(IntegerUtil.toBytes(c.getPort()));
		row.add(IntegerUtil.toBytes(c.getLocalPort()));
		row.add(LongUtil.toBytes(c.getNetInBytes()));
		row.add(LongUtil.toBytes(c.getNetOutBytes()));
		row.add(LongUtil.toBytes((TimeUtil.currentTimeMillis() - c
				.getStartupTime()) / 1000L));
		row.add(c.isClosed() ? "true".getBytes() : "false".getBytes());
		// boolean isRunning = c.isRunning();
		// row.add(isRunning ? "true".getBytes() : "false".getBytes());
		boolean isBorrowed = c.isBorrowed();
		row.add(isBorrowed ? "true".getBytes() : "false".getBytes());
		int writeQueueSize = 0;
		String schema = "";
		String charsetInf = "";
		String txLevel = "";
		String txAutommit = "";

		if (c instanceof MySQLConnection) {
			MySQLConnection mysqlC = (MySQLConnection) c;
			writeQueueSize = mysqlC.getWriteQueue().size();
			schema = mysqlC.getSchema();
			charsetInf = mysqlC.getCharset() + ":" + mysqlC.getCharsetIndex();
			txLevel = mysqlC.getTxIsolation() + "";
			txAutommit = mysqlC.isAutocommit() + "";
		}
		row.add(IntegerUtil.toBytes(writeQueueSize));
		row.add(schema.getBytes());
		row.add(charsetInf.getBytes());
		row.add(txLevel.getBytes());
		row.add(txAutommit.getBytes());
		return row;
	}
}