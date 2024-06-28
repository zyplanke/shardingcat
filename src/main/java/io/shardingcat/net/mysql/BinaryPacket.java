
package io.shardingcat.net.mysql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import io.shardingcat.backend.mysql.BufferUtil;
import io.shardingcat.backend.mysql.StreamUtil;
import io.shardingcat.net.BackendAIOConnection;
import io.shardingcat.net.FrontendConnection;

/**
 * @author shardingcat
 */
public class BinaryPacket extends MySQLPacket {
    public static final byte OK = 1;
    public static final byte ERROR = 2;
    public static final byte HEADER = 3;
    public static final byte FIELD = 4;
    public static final byte FIELD_EOF = 5;
    public static final byte ROW = 6;
    public static final byte PACKET_EOF = 7;

    public byte[] data;

    public void read(InputStream in) throws IOException {
        packetLength = StreamUtil.readUB3(in);
        packetId = StreamUtil.read(in);
        byte[] ab = new byte[packetLength];
        StreamUtil.read(in, ab, 0, ab.length);
        data = ab;
    }

    @Override
    public ByteBuffer write(ByteBuffer buffer, FrontendConnection c,boolean writeSocketIfFull) {
        buffer = c.checkWriteBuffer(buffer, c.getPacketHeaderSize(),writeSocketIfFull);
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer = c.writeToBuffer(data, buffer);
        return buffer;
    }
    @Override
    public void write(BackendAIOConnection c) {
        ByteBuffer buffer = c.allocate();
        buffer=  c.checkWriteBuffer(buffer,c.getPacketHeaderSize()+calcPacketSize(),false);
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer.put(data);
        c.write(buffer);
    }

    @Override
    public int calcPacketSize() {
        return data == null ? 0 : data.length;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Binary Packet";
    }

}