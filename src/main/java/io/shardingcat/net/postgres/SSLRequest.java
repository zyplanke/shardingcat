
package io.shardingcat.net.postgres;

/**
 * <pre>
 * SSLRequest (F) 
 * Int32(8) Length of message contents in bytes, including self. 
 * Int32(80877103) The SSL request code. The value is chosen to contain 1234 in 
 *                 the most significant 16 bits, and 5679 in the least 16 significant 
 *                 bits. (To avoid confusion, this code must not be the same as any 
 *                 protocol version number.)
 * </pre>
 * 
 * @author shardingcat
 */
public class SSLRequest extends PostgresPacket {

}