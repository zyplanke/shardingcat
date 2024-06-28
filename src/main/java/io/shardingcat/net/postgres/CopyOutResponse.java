
package io.shardingcat.net.postgres;

/**
 * <pre>
 * CopyOutResponse (B) 
 * Byte1('H') Identifies the message as a Start Copy Out response. 
 *            This message will be followed by copy-out data. Int32 Length of
 *            message contents in bytes, including self. 
 * Int8 0 indicates the overall COPY format is textual (rows separated by 
 *      newlines, columns separated by separator characters, etc). 1 indicates 
 *      the overall copy format is binary(similar to DataRow format). 
 *      See COPY for more information. 
 * Int16 The number of columns in the data to be copied (denoted N below). 
 * Int16[N] The format codes to be used for each column. Each must presently 
 *          be zero(text) or one (binary). All must be zero if the overall 
 *          copy format is textual.
 * </pre>
 * 
 * @author shardingcat
 */
public class CopyOutResponse extends PostgresPacket {

}