
package io.shardingcat.manager.response;

import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.datasource.PhysicalDBPool;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.OkPacket;
import io.shardingcat.route.parser.ManagerParseStop;
import io.shardingcat.route.parser.util.Pair;
import io.shardingcat.util.FormatUtil;
import io.shardingcat.util.TimeUtil;

/**
 * 暂停数据节点心跳检测
 * 
 * @author shardingcat
 */
public final class StopHeartbeat {

    private static final Logger logger = LoggerFactory.getLogger(StopHeartbeat.class);

    public static void execute(String stmt, ManagerConnection c) {
        int count = 0;
        Pair<String[], Integer> keys = ManagerParseStop.getPair(stmt);
        if (keys.getKey() != null && keys.getValue() != null) {
            long time = keys.getValue().intValue() * 1000L;
            Map<String, PhysicalDBPool> dns = ShardingCatServer.getInstance().getConfig().getDataHosts();
            for (String key : keys.getKey()) {
            	PhysicalDBPool dn = dns.get(key);
                if (dn != null) {
                    dn.getSource().setHeartbeatRecoveryTime(TimeUtil.currentTimeMillis() + time);
                    ++count;
                    StringBuilder s = new StringBuilder();
                    s.append(dn.getHostName()).append(" stop heartbeat '");
                    logger.warn(s.append(FormatUtil.formatTime(time, 3)).append("' by manager.").toString());
                }
            }
        }
        OkPacket packet = new OkPacket();
        packet.packetId = 1;
        packet.affectedRows = count;
        packet.serverStatus = 2;
        packet.write(c);
    }

}