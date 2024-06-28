
package io.shardingcat.net.handler;

/**
 * SQL预处理处理器
 * 
 * @author shardingcat, CrazyPig
 */
public interface FrontendPrepareHandler {
    
    void prepare(String sql);
    
    void sendLongData(byte[] data);

    void reset(byte[] data);
    
    void execute(byte[] data);

    void close(byte[] data);

    void clear();

}