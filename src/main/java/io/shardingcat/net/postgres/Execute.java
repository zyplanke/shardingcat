
package io.shardingcat.net.postgres;

/**
 * <pre>
 * Execute (F) 
 * Byte1('E') Identifies the message as an Execute command.
 * Int32 Length of message contents in bytes, including self. 
 * String The name of the portal to execute (an empty string 
 *        selects the unnamed portal). 
 * Int32 Maximum number of rows to return, if portal contains a
 *       query that returns rows (ignored otherwise). 
 *       Zero denotes "no limit".
 * </pre>
 * 
 * @author shardingcat
 */
public class Execute extends PostgresPacket {

}