
package io.shardingcat.net.postgres;

/**
 * <pre>
 * CopyData (F & B) 
 * Byte1('d') Identifies the message as COPY data. 
 * Int32 Length of message contents in bytes, including self. 
 * Byten Data that forms part of a COPY data stream. Messages sent from the backend will
 *       always correspond to single data rows, but messages sent by frontends
 *       might divide the data stream arbitrarily.
 * </pre>
 * 
 * @author shardingcat
 */
public class CopyData extends PostgresPacket {

}