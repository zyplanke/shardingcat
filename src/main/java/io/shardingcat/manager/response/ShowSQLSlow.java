
package io.shardingcat.manager.response;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import io.shardingcat.backend.mysql.PacketUtil;
import io.shardingcat.config.Fields;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.statistic.SQLRecord;
import io.shardingcat.statistic.stat.UserStat;
import io.shardingcat.statistic.stat.UserStatAnalyzer;
import io.shardingcat.util.LongUtil;
import io.shardingcat.util.StringUtil;

/**
 * 查询每个用户的执行时间超过设定阈值的SQL, 默认TOP10
 * 
 * @author shardingcat
 * @author zhuam
 */
public final class ShowSQLSlow {

    private static final int FIELD_COUNT = 5;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    
    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        
        fields[i] = PacketUtil.getField("USER", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("DATASOURCE", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("START_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("EXECUTE_TIME", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("SQL", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void execute(ManagerConnection c, boolean isClear) {
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
        Map<String, UserStat> statMap = UserStatAnalyzer.getInstance().getUserStatMap();
        for (UserStat userStat : statMap.values()) {
        	String user = userStat.getUser();
            List<SQLRecord> keyList = userStat.getSqlRecorder().getRecords();
            for (SQLRecord key : keyList) {
                if (key != null) {
                    RowDataPacket row = getRow(user, key, c.getCharset());
                    row.packetId = ++packetId;
                    buffer = row.write(buffer, c,true);
                }
            }
            
            if ( isClear ) {
            	userStat.getSqlRecorder().clear();//读取慢SQL后，清理
            }
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c,true);

        // write buffer
        c.write(buffer);
    }

    private static RowDataPacket getRow(String user, SQLRecord sql, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add( StringUtil.encode(user, charset) );
        row.add( StringUtil.encode(sql.dataNode, charset) );
        row.add( LongUtil.toBytes(sql.startTime) );
        row.add( LongUtil.toBytes(sql.executeTime) );
        row.add( StringUtil.encode(sql.statement, charset) );
        return row;
    }

}