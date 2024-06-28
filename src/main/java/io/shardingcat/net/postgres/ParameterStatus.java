
package io.shardingcat.net.postgres;

/**
 * <pre>
 * ParameterStatus (B) 
 * Byte1('S') Identifies the message as a run-time parameter status report. 
 * Int32 Length of message contents in bytes,including self. 
 * String The name of the run-time parameter being reported.
 * String The current value of the parameter.
 * </pre>
 * 
 * @author shardingcat
 */
public class ParameterStatus extends PostgresPacket {

}