
package io.shardingcat.net.postgres;

/**
 * <pre>
 * NotificationResponse (B) 
 * Byte1('A') Identifies the message as a notification response. 
 * Int32 Length of message contents in bytes,including self. 
 * Int32 The process ID of the notifying backend process.
 * String The name of the channel that the notify has been raised on. 
 * String The "payload" string passed from the notifying process.
 * </pre>
 * 
 * @author shardingcat
 */
public class NotificationResponse extends PostgresPacket {

}