
package io.shardingcat;



import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.shardingcat.config.loader.zkprocess.comm.ZkConfig;
import io.shardingcat.config.model.SystemConfig;

/**
 * @author shardingcat
 */
public final class ShardingCatStartup {
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardingCatStartup.class);
    public static void main(String[] args) {
        //use zk ?
        ZkConfig.getInstance().initZk();
        try {
            String home = SystemConfig.getHomePath();
            if (home == null) {
                System.out.println(SystemConfig.SYS_HOME + "  is not set.");
                System.exit(-1);
            }
            // init
            ShardingCatServer server = ShardingCatServer.getInstance();
            server.beforeStart();

            // startup
            server.startup();
            System.out.println("ShardingCat Server startup successfully. see logs in logs/shardingcat.log");

        } catch (Exception e) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            LOGGER.error(sdf.format(new Date()) + " startup error", e);
            System.exit(-1);
        }
    }
}
