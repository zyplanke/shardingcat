
package io.shardingcat.net.postgres;

/**
 * <pre>
 * PortalSuspended (B) 
 * Byte1('s') Identifies the message as a portal-suspended indicator. Note this 
 *            only appears if an Execute message's row-count limit was reached. 
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class PortalSuspended extends PostgresPacket {

}