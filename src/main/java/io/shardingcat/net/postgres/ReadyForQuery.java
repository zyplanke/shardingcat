
package io.shardingcat.net.postgres;

/**
 * <pre>
 * ReadyForQuery (B) 
 * Byte1('Z') Identifies the message type. ReadyForQuery is sent whenever the 
 *            backend is ready for a new query cycle. 
 * Int32(5) Length of message contents in bytes, including self. 
 * Byte1 Current backend transaction status indicator. Possible values are 'I' 
 *       if idle(not in a transaction block); 'T' if in a transaction block; 
 *       or 'E' if in a failed transaction block (queries will be rejected until
 *       block is ended).
 * </pre>
 * 
 * @author shardingcat
 */
public class ReadyForQuery extends PostgresPacket {

}