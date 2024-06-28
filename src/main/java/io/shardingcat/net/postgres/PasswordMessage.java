
package io.shardingcat.net.postgres;

/**
 * <pre>
 * PasswordMessage (F) 
 * Byte1('p') Identifies the message as a password response. Note that this is 
 *            also used for GSSAPI and SSPI response messages (which is really
 *            a design error, since the contained data is not a null-terminated 
 *            string in that case, but can be arbitrary binary data).
 * Int32 Length of message contents in bytes, including self. 
 * String The password(encrypted, if requested).
 * </pre>
 * 
 * @author shardingcat
 */
public class PasswordMessage extends PostgresPacket {

}