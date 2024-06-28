
package io.shardingcat.manager.response;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import io.shardingcat.route.parser.ManagerParseShow;
import io.shardingcat.statistic.DataSourceSyncRecorder;
import io.shardingcat.statistic.DataSourceSyncRecorder.Record;
import io.shardingcat.util.LongUtil;
import io.shardingcat.util.StringUtil;


/**
 * @author songwie
 */
public class ShowDatasourceSynDetail {

	private static final int FIELD_COUNT = 8;
	private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();

	
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

		fields[i] = PacketUtil.getField("TIME", Fields.FIELD_TYPE_DATETIME);
		fields[i++].packetId = ++packetId;
		
		fields[i] = PacketUtil.getField("Seconds_Behind_Master", Fields.FIELD_TYPE_LONG);
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
		
		String name = ManagerParseShow.getWhereParameter(stmt);
		for (RowDataPacket row : getRows(name,c.getCharset())) {
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
	 
	private static List<RowDataPacket> getRows(String name,String charset) {
		List<RowDataPacket> list = new LinkedList<RowDataPacket>();
		ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
		// host nodes
		Map<String, PhysicalDBPool> dataHosts = conf.getDataHosts();
		for (PhysicalDBPool pool : dataHosts.values()) {
			for (PhysicalDatasource ds : pool.getAllDataSources()) {
				DBHeartbeat hb = ds.getHeartbeat();
				DataSourceSyncRecorder record = hb.getAsynRecorder();
				Map<String, String> states = record.getRecords();
				if(name.equals(ds.getName())){
					List<Record> data = record.getAsynRecords();
					for(Record r : data){
						RowDataPacket row = new RowDataPacket(FIELD_COUNT);

						row.add(StringUtil.encode(ds.getName(),charset));
						row.add(StringUtil.encode(ds.getConfig().getIp(),charset));
						row.add(LongUtil.toBytes(ds.getConfig().getPort()));
						row.add(StringUtil.encode(states.get("Master_Host"),charset));
						row.add(LongUtil.toBytes(Long.valueOf(states.get("Master_Port"))));
						row.add(StringUtil.encode(states.get("Master_Use"),charset));
						//DateFormat非线程安全
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String time = sdf.format(new Date(r.getTime()));
						row.add(StringUtil.encode(time,charset));
						row.add(LongUtil.toBytes((Long)r.getValue()));

						list.add(row);
					}
					break;
				}

			}
		}
		return list;
	}
}
