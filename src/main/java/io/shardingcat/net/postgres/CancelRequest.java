
package io.shardingcat.net.postgres;

/**
 * <pre>
 * CancelRequest (F) 
 * Int32(16) Length of message contents in bytes,including self. 
 * Int32(80877102) The cancel request code. The value is chosen to 
 *                 contain 1234 in the most significant 16 bits, and 
 *                 5678 in the least 16 significant bits. (To avoid 
 *                 confusion, this code must not be the same as any 
 *                 protocol version number.) 
 * Int32 The process ID of the target backend. 
 * Int32 The secret key for the target backend.
 * </pre>
 * 
 * @author shardingcat
 */
public class CancelRequest extends PostgresPacket {

}