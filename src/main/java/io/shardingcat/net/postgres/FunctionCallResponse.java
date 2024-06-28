
package io.shardingcat.net.postgres;

/**
 * <pre>
 * FunctionCallResponse (B) 
 * Byte1('V') Identifies the message as a function call result. 
 * Int32 Length of message contents in bytes, including self.
 * Int32 The length of the function result value, in bytes (this count does
 *       not include itself). Can be zero. As a special case, -1 indicates a 
 *       NULL function result. No value bytes follow in the NULL case. 
 * Byten The value of the function result, in the format indicated by the 
 *       associated format code. n is the above length.
 * </pre>
 * 
 * @author shardingcat
 */
public class FunctionCallResponse extends PostgresPacket {

}