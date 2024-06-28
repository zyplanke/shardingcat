
package io.shardingcat.net.mysql;

import java.nio.ByteBuffer;

import io.shardingcat.backend.mysql.BufferUtil;
import io.shardingcat.backend.mysql.MySQLMessage;
import io.shardingcat.net.BackendAIOConnection;

/**
 * From client to server when the client do heartbeat between shardingcat cluster.
 * 
 * <pre>
 * Bytes         Name
 * -----         ----
 * 1             command
 * n             id
 * 
 * @author shardingcat
 */
public class HeartbeatPacket extends MySQLPacket {

    public byte command;
    public long id;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        command = mm.read();
        id = mm.readLength();
    }

    @Override
    public void write(BackendAIOConnection c) {
        ByteBuffer buffer = c.allocate();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer.put(command);
        BufferUtil.writeLength(buffer, id);
        c.write(buffer);
    }

    @Override
    public int calcPacketSize() {
        return 1 + BufferUtil.getLength(id);
    }

    @Override
    protected String getPacketInfo() {
        return "ShardingCat Heartbeat Packet";
    }

}