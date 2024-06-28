
package io.shardingcat.net.postgres;

/**
 * <pre>
 * AuthenticationKerberosV5 (B) 
 * Byte1('R') Identifies the message as an authentication request. 
 * Int32(8) Length of message contents in bytes, including self. 
 * Int32(2) Specifies that Kerberos V5 authentication is required.
 * </pre>
 * 
 * @author shardingcat
 */
public class AuthenticationKerberosV5 extends PostgresPacket {

}