
package io.shardingcat.net.postgres;

/**
 * <pre>
 * AuthenticationMD5Password (B)
 * Byte1('R') Identifies the message as an authentication request. 
 * Int32(12) Length of message contents in bytes, including self. 
 * Int32(5) Specifies that an MD5-encrypted password is required. 
 * Byte4 The salt to use when encrypting the password.
 * </pre>
 * 
 * @author shardingcat
 */
public class AuthenticationMD5Password extends PostgresPacket {

}