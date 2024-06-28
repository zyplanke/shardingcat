
package io.shardingcat.net.postgres;

/**
 * <pre>
 * BackendKeyData (B) 
 * Byte1('K') Identifies the message as cancellation key data. 
 *            The frontend must save these values if it wishes to be able to
 *            issue CancelRequest messages later. 
 * Int32(12) Length of message contents in bytes, including self. 
 * Int32 The process ID of this backend. 
 * Int32 The secret key of this backend.
 * </pre>
 * 
 * @author shardingcat
 */
public class BackendKeyData extends PostgresPacket {

}