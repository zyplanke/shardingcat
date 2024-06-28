
package io.shardingcat.manager.response;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.datasource.PhysicalDBNode;
import io.shardingcat.backend.datasource.PhysicalDBPool;
import io.shardingcat.config.ErrorCode;
import io.shardingcat.config.ShardingCatConfig;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.OkPacket;

/**
 * @author shardingcat
 */
public class ClearSlow {

    public static void dataNode(ManagerConnection c, String name) {
    	PhysicalDBNode dn = ShardingCatServer.getInstance().getConfig().getDataNodes().get(name);
    	PhysicalDBPool ds = null;
        if (dn != null && ((ds = dn.getDbPool())!= null)) {
           // ds.getSqlRecorder().clear();
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Invalid DataNode:" + name);
        }
    }

    public static void schema(ManagerConnection c, String name) {
        ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
        SchemaConfig schema = conf.getSchemas().get(name);
        if (schema != null) {
//            Map<String, MySQLDataNode> dataNodes = conf.getDataNodes();
//            for (String n : schema.getAllDataNodes()) {
//                MySQLDataNode dn = dataNodes.get(n);
//                MySQLDataSource ds = null;
//                if (dn != null && (ds = dn.getSource()) != null) {
//                    ds.getSqlRecorder().clear();
//                }
//            }
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Invalid Schema:" + name);
        }
    }

}