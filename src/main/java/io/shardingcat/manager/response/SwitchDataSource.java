
package io.shardingcat.manager.response;

import java.util.Map;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.datasource.PhysicalDBPool;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.OkPacket;
import io.shardingcat.route.parser.ManagerParseSwitch;
import io.shardingcat.route.parser.util.Pair;

/**
 * 切换数据节点的数据源
 * 
 * @author shardingcat
 */
public final class SwitchDataSource {

    public static void response(String stmt, ManagerConnection c) {
        int count = 0;
        Pair<String[], Integer> pair = ManagerParseSwitch.getPair(stmt);
        Map<String, PhysicalDBPool> dns = ShardingCatServer.getInstance().getConfig().getDataHosts();
        Integer idx = pair.getValue();
        for (String key : pair.getKey()) {
        	PhysicalDBPool dn = dns.get(key);
            if (dn != null) {
                int m = dn.getActivedIndex();
                int n = (idx == null) ? dn.next(m) : idx.intValue();
                if (dn.switchSource(n, false, "MANAGER")) {
                    ++count;
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