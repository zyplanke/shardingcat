
package io.shardingcat.net.handler;

/**
 * load data infile
 * 
 * @author magicdoom
 */
public interface LoadDataInfileHandler
{

    void start(String sql);

    void handle(byte[] data);

    void end(byte packID);

    void clear();

    byte getLastPackId();

    boolean isStartLoadData();

}