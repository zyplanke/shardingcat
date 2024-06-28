
package io.shardingcat.manager.response;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.datasource.PhysicalDBPool;
import io.shardingcat.backend.datasource.PhysicalDatasource;
import io.shardingcat.backend.heartbeat.DBHeartbeat;
import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.config.Fields;
import io.shardingcat.config.ShardingCatConfig;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.statistic.DataSourceSyncRecorder;
import io.shardingcat.util.LongUtil;
import io.shardingcat.util.StringUtil;


/**
 * @author songwie
 */
public class ShowDatasourceSyn {

	private static final int FIELD_COUNT = 12;
	private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	
	static {
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;

		fields[i] = PacketUtil.getField("name", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("host", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("port", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("Master_Host", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("Master_Port", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("Master_Use", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("Seconds_Behind_Master", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("Slave_IO_Running", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("Slave_SQL_Running", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("Slave_IO_State", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("Connect_Retry", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("Last_IO_Error", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		
		eof.packetId = ++packetId;
	}

	public static void response(ManagerConnection c,String stmt) {
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
		
		for (RowDataPacket row : getRows(c.getCharset())) {
			row.packetId = ++packetId;
			buffer = row.write(buffer, c,true);
		}

		// write last eof
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c,true);

		// post write
		c.write(buffer);
	}
 
	private static List<RowDataPacket> getRows(String charset) {
		List<RowDataPacket> list = new LinkedList<RowDataPacket>();
		ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
		// host nodes
		Map<String, PhysicalDBPool> dataHosts = conf.getDataHosts();
		for (PhysicalDBPool pool : dataHosts.values()) {
			for (PhysicalDatasource ds : pool.getAllDataSources()) {
				DBHeartbeat hb = ds.getHeartbeat();
				DataSourceSyncRecorder record = hb.getAsynRecorder();
				Map<String, String> states = record.getRecords();
				RowDataPacket row = new RowDataPacket(FIELD_COUNT);
				if(!states.isEmpty()){
					row.add(StringUtil.encode(ds.getName(),charset));
					row.add(StringUtil.encode(ds.getConfig().getIp(),charset));
					row.add(LongUtil.toBytes(ds.getConfig().getPort()));
					row.add(StringUtil.encode(states.get("Master_Host"),charset));
					row.add(LongUtil.toBytes(Long.valueOf(states.get("Master_Port"))));
					row.add(StringUtil.encode(states.get("Master_Use"),charset));
					String secords = states.get("Seconds_Behind_Master");
					row.add(secords==null?null:LongUtil.toBytes(Long.valueOf(secords)));
					row.add(StringUtil.encode(states.get("Slave_IO_Running"),charset));
					row.add(StringUtil.encode(states.get("Slave_SQL_Running"),charset));
					row.add(StringUtil.encode(states.get("Slave_IO_State"),charset));
					row.add(LongUtil.toBytes(Long.valueOf(states.get("Connect_Retry"))));
					row.add(StringUtil.encode(states.get("Last_IO_Error"),charset));

					list.add(row);
				}
			}
		}
		return list;
	}

}