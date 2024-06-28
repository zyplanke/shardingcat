
package io.shardingcat.net.mysql;

/**
 * @author shardingcat
 */
public class PingPacket extends MySQLPacket {
    public static final byte[] PING = new byte[] { 1, 0, 0, 0, 14 };

    @Override
    public int calcPacketSize() {
        return 1;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Ping Packet";
    }

}