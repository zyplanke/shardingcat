
package io.shardingcat.net.postgres;

/**
 * <pre>
 * AuthenticationSCMCredential (B) 
 * Byte1('R') Identifies the message as an authentication request. 
 * Int32(8) Length of message contents in bytes, including self. 
 * Int32(6) Specifies that an SCM credentials message is required.
 * </pre>
 * 
 * @author shardingcat
 */
public class AuthenticationSCMCredential extends PostgresPacket {

}