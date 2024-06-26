
package io.shardingcat.net.mysql;

import java.nio.ByteBuffer;

import io.shardingcat.backend.mysql.BufferUtil;
import io.shardingcat.net.FrontendConnection;

/**
 * load data local infile 向客户端请求发送文件用
 */
public class RequestFilePacket extends MySQLPacket
{
    public static final byte FIELD_COUNT = (byte) 251;
    public byte command = FIELD_COUNT;
    public byte[] fileName;


    @Override
    public ByteBuffer write(ByteBuffer buffer, FrontendConnection c, boolean writeSocketIfFull)
    {
        int size = calcPacketSize();
        buffer = c.checkWriteBuffer(buffer, c.getPacketHeaderSize() + size, writeSocketIfFull);
        BufferUtil.writeUB3(buffer, size);
        buffer.put(packetId);
        buffer.put(command);
        if (fileName != null)
        {

            buffer.put(fileName);

        }

        c.write(buffer);

        return buffer;
    }

    @Override
    public int calcPacketSize()
    {
        return fileName == null ? 1 : 1 + fileName.length;
    }

    @Override
    protected String getPacketInfo()
    {
        return "MySQL Request File Packet";
    }


}