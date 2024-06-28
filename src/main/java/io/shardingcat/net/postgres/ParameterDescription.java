
package io.shardingcat.net.postgres;

/**
 * <pre>
 * ParameterDescription (B) 
 * Byte1('t') Identifies the message as a parameter description. 
 * Int32 Length of message contents in bytes, including self.
 * Int16 The number of parameters used by the statement (can be zero). 
 *       Then,for each parameter, there is the following: 
 * Int32 Specifies the object ID of the parameter data type.
 * </pre>
 * 
 * @author shardingcat
 */
public class ParameterDescription extends PostgresPacket {

}