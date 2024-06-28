
package io.shardingcat.net.postgres;

/**
 * <pre>
 * AuthenticationGSSContinue (B) 
 * Byte1('R') Identifies the message as an authentication request. 
 * Int32 Length of message contents in bytes, including self. 
 * Int32(8) Specifies that this message contains GSSAPI or SSPI data. 
 * Byten GSSAPI or SSPI authentication data.
 * </pre>
 * 
 * @author shardingcat
 */
public class AuthenticationGSSContinue extends PostgresPacket {

}