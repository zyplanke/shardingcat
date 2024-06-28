
package io.shardingcat.manager.response;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.net.mysql.OkPacket;

/**
 * @author shardingcat
 */
public final class ReloadUser {

    private static final Logger logger = LoggerFactory.getLogger(ReloadUser.class);

    public static void execute(ManagerConnection c) {
        boolean status = false;
        if (status) {
            StringBuilder s = new StringBuilder();
            s.append(c).append("Reload userConfig success by manager");
            logger.warn(s.toString());
            OkPacket ok = new OkPacket();
            ok.packetId = 1;
            ok.affectedRows = 1;
            ok.serverStatus = 2;
            ok.message = "Reload userConfig success".getBytes();
            ok.write(c);
        } else {
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}