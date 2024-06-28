
package io.shardingcat.net.postgres;

/**
 * <pre>
 * AuthenticationCleartextPassword (B)
 * Byte1('R') Identifies the message as an authentication request. 
 * Int32(8) Length of message contents in bytes, including self. 
 * Int32(3) Specifies that a clear-text password is required.
 * </pre>
 * 
 * @author shardingcat
 */
public class AuthenticationCleartextPassword extends PostgresPacket {

}