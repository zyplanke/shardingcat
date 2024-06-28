
package io.shardingcat.net.postgres;

/**
 * <pre>
 * Query (F) 
 * Byte1('Q') Identifies the message as a simple query. 
 * Int32 Length of message contents in bytes, including self. 
 * String The query string itself.
 * </pre>
 * 
 * @author shardingcat
 */
public class Query extends PostgresPacket {

}