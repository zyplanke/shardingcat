
package io.shardingcat.manager.response;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.datasource.PhysicalDBNode;
import io.shardingcat.backend.datasource.PhysicalDatasource;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.config.Fields;
import io.shardingcat.config.ShardingCatConfig;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.util.IntegerUtil;
import io.shardingcat.util.LongUtil;
import io.shardingcat.util.StringUtil;

/**
 * 查看数据源信息
 * 
 * @author shardingcat
 * @author shardingcat
 */
public final class ShowDataSource {

	private static final int FIELD_COUNT = 12;
	private static final ResultSetHeaderPacket header = PacketUtil
			.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();
	static {
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;

		fields[i] = PacketUtil.getField("DATANODE",
				Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("TYPE", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("PORT", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("W/R", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("ACTIVE", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("IDLE", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("SIZE", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("EXECUTE", Fields.FIELD_TYPE_LONGLONG);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("READ_LOAD", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("WRITE_LOAD", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		eof.packetId = ++packetId;
	}

	public static void execute(ManagerConnection c, String name) {
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
		ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
		Map<String, List<PhysicalDatasource>> dataSources = new HashMap<String, List<PhysicalDatasource>>();
		if (null != name) {
			PhysicalDBNode dn = conf.getDataNodes().get(name);
			if (dn != null) {
				List<PhysicalDatasource> dslst = new LinkedList<PhysicalDatasource>();
				dslst.addAll(dn.getDbPool().getAllDataSources());
				dataSources.put(dn.getName(), dslst);
			}

		} else {
			// add all

			for (PhysicalDBNode dn : conf.getDataNodes().values()) {
				List<PhysicalDatasource> dslst = new LinkedList<PhysicalDatasource>();
				dslst.addAll(dn.getDbPool().getAllDataSources());
				dataSources.put(dn.getName(), dslst);
			}

		}

		for (Map.Entry<String, List<PhysicalDatasource>> dsEntry : dataSources
				.entrySet()) {
			String dnName = dsEntry.getKey();
			for (PhysicalDatasource ds : dsEntry.getValue()) {
				RowDataPacket row = getRow(dnName, ds, c.getCharset());
				row.packetId = ++packetId;
				buffer = row.write(buffer, c,true);
			}
		}

		// write last eof
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c,true);

		// post write
		c.write(buffer);
	}

	private static RowDataPacket getRow(String dataNode, PhysicalDatasource ds,
			String charset) {
		RowDataPacket row = new RowDataPacket(FIELD_COUNT);
		row.add(StringUtil.encode(dataNode, charset));
		row.add(StringUtil.encode(ds.getName(), charset));
		row.add(StringUtil.encode(ds.getConfig().getDbType(), charset));
		row.add(StringUtil.encode(ds.getConfig().getIp(), charset));
		row.add(IntegerUtil.toBytes(ds.getConfig().getPort()));
		row.add(StringUtil.encode(ds.isReadNode() ? "R" : "W", charset));
		row.add(IntegerUtil.toBytes(ds.getActiveCount()));
		row.add(IntegerUtil.toBytes(ds.getIdleCount()));
		row.add(IntegerUtil.toBytes(ds.getSize()));
		row.add(LongUtil.toBytes(ds.getExecuteCount()));
		row.add(LongUtil.toBytes(ds.getReadCount()));
		row.add(LongUtil.toBytes(ds.getWriteCount()));
		return row;
	}

}