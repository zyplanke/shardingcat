
package io.shardingcat.manager;

import java.io.IOException;
import java.nio.channels.NetworkChannel;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.config.ShardingCatPrivileges;
import io.shardingcat.net.FrontendConnection;
import io.shardingcat.net.factory.FrontendConnectionFactory;

/**
 * @author shardingcat
 */
public class ManagerConnectionFactory extends FrontendConnectionFactory {

    @Override
    protected FrontendConnection getConnection(NetworkChannel channel) throws IOException {
        ManagerConnection c = new ManagerConnection(channel);
        ShardingCatServer.getInstance().getConfig().setSocketParams(c, true);
        c.setPrivileges(ShardingCatPrivileges.instance());
        c.setQueryHandler(new ManagerQueryHandler(c));
        return c;
    }

}