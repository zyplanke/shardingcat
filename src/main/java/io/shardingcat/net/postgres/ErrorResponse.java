
package io.shardingcat.net.postgres;

/**
 * <pre>
 * ErrorResponse (B) 
 * Byte1('E') Identifies the message as an error. 
 * Int32 Length of message contents in bytes, including self. 
 *       The message body consists of one or more identified fields, 
 *       followed by a zero byte as a terminator. Fields can appear 
 *       in any order. For each field there is the following: 
 * Byte1 A code identifying the field type; if zero, this is the
 *       message terminator and no string follows. The presently defined 
 *       field types are listed in Section 46.6. Since more field types 
 *       might be added in future, frontends should silently ignore 
 *       fields of unrecognized type.
 * String The field value.
 * </pre>
 * 
 * @author shardingcat
 */
public class ErrorResponse extends PostgresPacket {

}