
package io.shardingcat.net.postgres;

/**
 * <pre>
 * AuthenticationGSS (B) 
 * Byte1('R') Identifies the message as an authentication request. 
 * Int32(8) Length of message contents in bytes, including self. 
 * Int32(7) Specifies that GSSAPI authentication is required.
 * </pre>
 * 
 * @author shardingcat
 */
public class AuthenticationGSS extends PostgresPacket {

}