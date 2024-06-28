
package io.shardingcat.server;

import java.io.IOException;
import java.nio.channels.NetworkChannel;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.config.ShardingCatPrivileges;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.net.FrontendConnection;
import io.shardingcat.net.factory.FrontendConnectionFactory;
import io.shardingcat.server.handler.ServerLoadDataInfileHandler;
import io.shardingcat.server.handler.ServerPrepareHandler;

/**
 * @author shardingcat
 */
public class ServerConnectionFactory extends FrontendConnectionFactory {

    @Override
    protected FrontendConnection getConnection(NetworkChannel channel) throws IOException {
        SystemConfig sys = ShardingCatServer.getInstance().getConfig().getSystem();
        ServerConnection c = new ServerConnection(channel);
        ShardingCatServer.getInstance().getConfig().setSocketParams(c, true);
        c.setPrivileges(ShardingCatPrivileges.instance());
        c.setQueryHandler(new ServerQueryHandler(c));
        c.setLoadDataInfileHandler(new ServerLoadDataInfileHandler(c));
        c.setPrepareHandler(new ServerPrepareHandler(c));
        c.setTxIsolation(sys.getTxIsolation());
        c.setSession2(new NonBlockingSession(c));
        return c;
    }

}