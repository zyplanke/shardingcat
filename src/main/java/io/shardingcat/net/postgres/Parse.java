
package io.shardingcat.net.postgres;

/**
 * <pre>
 * Parse (F) 
 * Byte1('P') Identifies the message as a Parse command. 
 * Int32 Length of message contents in bytes, including self. 
 * String The name of the destination prepared statement (an empty string 
 *        selects the unnamed prepared statement). 
 * String The query string to be parsed. 
 * Int16 The number of parameter data types specified (can be zero). Note 
 *       that this is not an indication of the number of parameters that 
 *       might appear in the query string, only the number that the frontend 
 *       wants to prespecify types for. Then, for each parameter, there is 
 *       the following: 
 * Int32 Specifies the object ID of the parameter data type. Placing a zero 
 *       here is equivalent to leaving the type unspecified.
 * </pre>
 * 
 * @author shardingcat
 */
public class Parse extends PostgresPacket {

}