
package io.shardingcat.net.postgres;

/**
 * <pre>
 * Close (F) 
 * Byte1('C') Identifies the message as a Close command. 
 * Int32 Length of message contents in bytes, including self. 
 * Byte1 'S' to close a prepared statement; or 'P' to close a portal. 
 * String The name of the prepared statement or portal to close (an 
 *        empty string selects the unnamed prepared statement or portal).
 * </pre>
 * 
 * @author shardingcat
 */
public class Close extends PostgresPacket {

}