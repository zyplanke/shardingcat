
package io.shardingcat.net.postgres;

/**
 * <pre>
 * AuthenticationSSPI (B) 
 * Byte1('R') Identifies the message as an authentication request. 
 * Int32(8) Length of message contents in bytes, including self. 
 * Int32(9) Specifies that SSPI authentication is required.
 * </pre>
 * 
 * @author shardingcat
 */
public class AuthenticationSSPI extends PostgresPacket {

}