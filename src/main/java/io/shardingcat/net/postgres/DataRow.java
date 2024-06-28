
package io.shardingcat.net.postgres;

/**
 * <pre>
 * DataRow (B) 
 * Byte1('D') Identifies the message as a data row. 
 * Int32 Length of message contents in bytes, including self. 
 * Int16 The number of column values that follow (possibly zero). 
 *       Next, the following pair of fields appear for each column: 
 * Int32 The length of the column value, in bytes(this count does not 
 *       include itself). Can be zero. As a special case, -1 indicates 
 *       a NULL column value. No value bytes follow in the NULL case.
 * Byten The value of the column, in the format indicated by the associated
 *       format code. n is the above length.
 * </pre>
 * 
 * @author shardingcat
 */
public class DataRow extends PostgresPacket {

}