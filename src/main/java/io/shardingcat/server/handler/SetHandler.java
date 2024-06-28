
package io.shardingcat.server.handler;

import static io.shardingcat.server.parser.ServerParseSet.AUTOCOMMIT_OFF;
import static io.shardingcat.server.parser.ServerParseSet.AUTOCOMMIT_ON;
import static io.shardingcat.server.parser.ServerParseSet.CHARACTER_SET_CLIENT;
import static io.shardingcat.server.parser.ServerParseSet.CHARACTER_SET_CONNECTION;
import static io.shardingcat.server.parser.ServerParseSet.CHARACTER_SET_RESULTS;
import static io.shardingcat.server.parser.ServerParseSet.NAMES;
import static io.shardingcat.server.parser.ServerParseSet.TX_READ_COMMITTED;
import static io.shardingcat.server.parser.ServerParseSet.TX_READ_UNCOMMITTED;
import static io.shardingcat.server.parser.ServerParseSet.TX_REPEATED_READ;
import static io.shardingcat.server.parser.ServerParseSet.TX_SERIALIZABLE;
import static io.shardingcat.server.parser.ServerParseSet.XA_FLAG_OFF;
import static io.shardingcat.server.parser.ServerParseSet.XA_FLAG_ON;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.config.Isolations;
import io.shardingcat.net.mysql.OkPacket;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.server.parser.ServerParseSet;
import io.shardingcat.server.response.CharacterSet;
import io.shardingcat.util.SetIgnoreUtil;

/**
 * SET 语句处理
 * 
 * @author shardingcat
 * @author zhuam
 */
public final class SetHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SetHandler.class);
	
	private static final byte[] AC_OFF = new byte[] { 7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 };
		
	public static void handle(String stmt, ServerConnection c, int offset) {
		// System.out.println("SetHandler: "+stmt);
		int rs = ServerParseSet.parse(stmt, offset);
		switch (rs & 0xff) {
		case AUTOCOMMIT_ON:
			if (c.isAutocommit()) {
				c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			} else {
				c.setPreAcStates(true);
				c.commit();
				c.setAutocommit(true);
			}
			break;
		case AUTOCOMMIT_OFF: {
			if (c.isAutocommit()) {
				c.setAutocommit(false);
				c.setPreAcStates(false);
			}
			c.write(c.writeToBuffer(AC_OFF, c.allocate()));
			break;
		}
		case XA_FLAG_ON: {
			if (c.isAutocommit()) {
				c.writeErrMessage(ErrorCode.ERR_WRONG_USED,
						"set xa cmd on can't used in autocommit connection ");
				return;
			}
			c.getSession2().setXATXEnabled(true);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			break;
		}
		case XA_FLAG_OFF: {
			c.writeErrMessage(ErrorCode.ERR_WRONG_USED,
					"set xa cmd off not for external use ");
			return;
		}
		case TX_READ_UNCOMMITTED: {
			c.setTxIsolation(Isolations.READ_UNCOMMITTED);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			break;
		}
		case TX_READ_COMMITTED: {
			c.setTxIsolation(Isolations.READ_COMMITTED);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			break;
		}
		case TX_REPEATED_READ: {
			c.setTxIsolation(Isolations.REPEATED_READ);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			break;
		}
		case TX_SERIALIZABLE: {
			c.setTxIsolation(Isolations.SERIALIZABLE);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			break;
		}
		case NAMES:
			String charset = stmt.substring(rs >>> 8).trim();
		   int index=	charset.indexOf(",")  ;
			if(index>-1) {
				//支持rails框架自动生成的SET NAMES utf8,  @@SESSION.sql_auto_is_null = 0, @@SESSION.wait_timeout = 2147483, @@SESSION.sql_mode = 'STRICT_ALL_TABLES'
			charset=charset.substring(0,index)	;
			}
			if(charset.startsWith("'")&&charset.endsWith("'"))
			{
				charset=charset.substring(1,charset.length()-1)  ;
			}
			if (c.setCharset(charset)) {
				c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			} else {
				
				/**
				 * TODO：修复 phpAyAdmin's 的发包问题
				 * 如： SET NAMES 'utf8' COLLATE 'utf8_general_ci' 错误
				 */	
				int beginIndex = stmt.toLowerCase().indexOf("names");
				int endIndex = stmt.toLowerCase().indexOf("collate");
				if ( beginIndex > -1 && endIndex > -1 ) {					
					charset = stmt.substring(beginIndex + "names".length(), endIndex);					
					//重试一次
					if (c.setCharset( charset.trim() )) {
						c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
					} else {
						c.writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
					}	
					
				} else {				
					c.writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
				}
			}
			break;
		case CHARACTER_SET_CLIENT:
		case CHARACTER_SET_CONNECTION:
		case CHARACTER_SET_RESULTS:
			CharacterSet.response(stmt, c, rs);
			break;
		default:			
			 boolean ignore = SetIgnoreUtil.isIgnoreStmt(stmt);
             if ( !ignore ) {        	 
     			StringBuilder s = new StringBuilder();
    			logger.warn(s.append(c).append(stmt).append(" is not recoginized and ignored").toString());
             }
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
		}
	}

}
