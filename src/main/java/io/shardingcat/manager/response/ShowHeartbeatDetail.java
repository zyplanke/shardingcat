
package io.shardingcat.manager.response;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
import io.shardingcat.route.parser.ManagerParseHeartbeat;
import io.shardingcat.route.parser.util.Pair;
import io.shardingcat.statistic.HeartbeatRecorder;
import io.shardingcat.util.IntegerUtil;
import io.shardingcat.util.LongUtil;
import io.shardingcat.util.StringUtil;


/**
 * @author songwie
 */
public class ShowHeartbeatDetail {

	private static final int FIELD_COUNT = 6;
	private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
	private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket eof = new EOFPacket();
	
	static {
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;

		fields[i] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("TYPE", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("PORT", Fields.FIELD_TYPE_LONG);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("TIME", Fields.FIELD_TYPE_DATETIME);
		fields[i++].packetId = ++packetId;

		fields[i] = PacketUtil.getField("EXECUTE_TIME", Fields.FIELD_TYPE_VAR_STRING);
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
		Pair<String,String> pair = ManagerParseHeartbeat.getPair(stmt);
		String name = pair.getValue();
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
		String type = "";
		String ip = "";
		int port = 0;
		DBHeartbeat hb = null;

		Map<String, PhysicalDBPool> dataHosts = conf.getDataHosts();
		for (PhysicalDBPool pool : dataHosts.values()) {
			for (PhysicalDatasource ds : pool.getAllDataSources()) {
				if(name.equals(ds.getName())){
					hb = ds.getHeartbeat();
					type = ds.getConfig().getDbType();
					ip = ds.getConfig().getIp();
					port = ds.getConfig().getPort();
					break;
				}
			}
		}
		if(hb!=null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Queue<HeartbeatRecorder.Record> heatbeartRecorders = hb.getRecorder().getRecordsAll();  
			for(HeartbeatRecorder.Record record : heatbeartRecorders){
				RowDataPacket row = new RowDataPacket(FIELD_COUNT);
				row.add(StringUtil.encode(name,charset));
				row.add(StringUtil.encode(type,charset));
				row.add(StringUtil.encode(ip,charset));
				row.add(IntegerUtil.toBytes(port));
				long time = record.getTime();
				String timeStr = sdf.format(new Date(time));
				row.add(StringUtil.encode(timeStr,charset));
				row.add(LongUtil.toBytes(record.getValue()));

				list.add(row);
			}
		}else{
			RowDataPacket row = new RowDataPacket(FIELD_COUNT);
			row.add(null);
			row.add(null);
			row.add(null);
			row.add(null);
			row.add(null);
			row.add(null);
			list.add(row);
		}
		
		return list;
	}

}