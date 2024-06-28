
package io.shardingcat.net.postgres;

/**
 * <pre>
 * AuthenticationOk (B) 
 * Byte1('R') Identifies the message as an authentication request. 
 * Int32(8) Length of message contents in bytes, including self. 
 * Int32(0) Specifies that the authentication was successful.
 * </pre>
 * 
 * @author shardingcat
 */
public class AuthenticationOk extends PostgresPacket {

}