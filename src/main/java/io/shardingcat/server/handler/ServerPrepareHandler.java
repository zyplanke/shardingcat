
package io.shardingcat.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.escape.Escapers.Builder;

import io.shardingcat.backend.mysql.BindValue;
import io.shardingcat.backend.mysql.ByteUtil;
import io.shardingcat.backend.mysql.PreparedStatement;
import io.shardingcat.config.ErrorCode;
import io.shardingcat.config.Fields;
import io.shardingcat.net.handler.FrontendPrepareHandler;
import io.shardingcat.net.mysql.ExecutePacket;
import io.shardingcat.net.mysql.LongDataPacket;
import io.shardingcat.net.mysql.OkPacket;
import io.shardingcat.net.mysql.ResetPacket;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.server.response.PreparedStmtResponse;
import io.shardingcat.util.HexFormatUtil;

/**
 * @author shardingcat, CrazyPig, zhuam
 */
public class ServerPrepareHandler implements FrontendPrepareHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerPrepareHandler.class);
	
	private static Escaper varcharEscaper = null;
	
	static {
		Builder escapeBuilder = Escapers.builder();
		escapeBuilder.addEscape('\'', "\\'");
		escapeBuilder.addEscape('$', "\\$");
		varcharEscaper = escapeBuilder.build();
	}
	
    private ServerConnection source;
    private volatile long pstmtId;
    private Map<String, PreparedStatement> pstmtForSql;
    private Map<Long, PreparedStatement> pstmtForId;

    public ServerPrepareHandler(ServerConnection source) {
        this.source = source;
        this.pstmtId = 0L;
        this.pstmtForSql = new HashMap<String, PreparedStatement>();
        this.pstmtForId = new HashMap<Long, PreparedStatement>();
    }

    @Override
    public void prepare(String sql) {
    	
    	LOGGER.debug("use server prepare, sql: " + sql);
        PreparedStatement pstmt = null;
        if ((pstmt = pstmtForSql.get(sql)) == null) {
        	// 解析获取字段个数和参数个数
        	int columnCount = getColumnCount(sql);
        	int paramCount = getParamCount(sql);
            pstmt = new PreparedStatement(++pstmtId, sql, columnCount, paramCount);
            pstmtForSql.put(pstmt.getStatement(), pstmt);
            pstmtForId.put(pstmt.getId(), pstmt);
        }
        PreparedStmtResponse.response(pstmt, source);
    }
    
    @Override
	public void sendLongData(byte[] data) {
		LongDataPacket packet = new LongDataPacket();
		packet.read(data);
		long pstmtId = packet.getPstmtId();
		PreparedStatement pstmt = pstmtForId.get(pstmtId);
		if(pstmt != null) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("send long data to prepare sql : " + pstmtForId.get(pstmtId));
			}
			long paramId = packet.getParamId();
			try {
				pstmt.appendLongData(paramId, packet.getLongData());
			} catch (IOException e) {
				source.writeErrMessage(ErrorCode.ERR_FOUND_EXCEPION, e.getMessage());
			}
		}
	}

	@Override
	public void reset(byte[] data) {
		ResetPacket packet = new ResetPacket();
		packet.read(data);
		long pstmtId = packet.getPstmtId();
		PreparedStatement pstmt = pstmtForId.get(pstmtId);
		if(pstmt != null) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("reset prepare sql : " + pstmtForId.get(pstmtId));
			}
			pstmt.resetLongData();
			source.write(OkPacket.OK);
		} else {
			source.writeErrMessage(ErrorCode.ERR_FOUND_EXCEPION, "can not reset prepare statement : " + pstmtForId.get(pstmtId));
		}
	} 
    
    @Override
    public void execute(byte[] data) {
        long pstmtId = ByteUtil.readUB4(data, 5);
        PreparedStatement pstmt = null;
        if ((pstmt = pstmtForId.get(pstmtId)) == null) {
            source.writeErrMessage(ErrorCode.ER_ERROR_WHEN_EXECUTING_COMMAND, "Unknown pstmtId when executing.");
        } else {
            ExecutePacket packet = new ExecutePacket(pstmt);
            try {
                packet.read(data, source.getCharset());
            } catch (UnsupportedEncodingException e) {
                source.writeErrMessage(ErrorCode.ER_ERROR_WHEN_EXECUTING_COMMAND, e.getMessage());
                return;
            }
            BindValue[] bindValues = packet.values;
            // 还原sql中的动态参数为实际参数值
            String sql = prepareStmtBindValue(pstmt, bindValues);
            // 执行sql
            source.getSession2().setPrepared(true);
            if(LOGGER.isDebugEnabled()) {
            	LOGGER.debug("execute prepare sql: " + sql);
            }
            source.query( sql );
        }
    }
    
    
    @Override
    public void close(byte[] data) {
    	long pstmtId = ByteUtil.readUB4(data, 5); // 获取prepare stmt id
    	if(LOGGER.isDebugEnabled()) {
    		LOGGER.debug("close prepare stmt, stmtId = " + pstmtId);
    	}
    	PreparedStatement pstmt = pstmtForId.remove(pstmtId);
    	if(pstmt != null) {
    		pstmtForSql.remove(pstmt.getStatement());
    	}
    }
    
    @Override
    public void clear() {
    	this.pstmtForId.clear();
    	this.pstmtForSql.clear();
    }
    
    // TODO 获取预处理语句中column的个数
    private int getColumnCount(String sql) {
    	int columnCount = 0;
    	// TODO ...
    	return columnCount;
    }
    
    // 获取预处理sql中预处理参数个数
    private int getParamCount(String sql) {
    	char[] cArr = sql.toCharArray();
    	int count = 0;
    	for(int i = 0; i < cArr.length; i++) {
    		if(cArr[i] == '?') {
    			count++;
    		}
    	}
    	return count;
    }
    
    /**
     * 组装sql语句,替换动态参数为实际参数值
     * @param pstmt
     * @param bindValues
     * @return
     */
    private String prepareStmtBindValue(PreparedStatement pstmt, BindValue[] bindValues) {
    	String sql = pstmt.getStatement();
    	int[] paramTypes = pstmt.getParametersType();
    	
    	StringBuilder sb = new StringBuilder();
    	int idx = 0;
    	for(int i = 0, len = sql.length(); i < len; i++) {
    		char c = sql.charAt(i);
    		if(c != '?') {
    			sb.append(c);
    			continue;
    		}
    		// 处理占位符?
    		int paramType = paramTypes[idx];
    		BindValue bindValue = bindValues[idx];
    		idx++;
    		// 处理字段为空的情况
    		if(bindValue.isNull) {
    			sb.append("NULL");
    			continue;
    		}
    		// 非空情况, 根据字段类型获取值
    		switch(paramType & 0xff) {
    		case Fields.FIELD_TYPE_TINY:
    			sb.append(String.valueOf(bindValue.byteBinding));
    			break;
    		case Fields.FIELD_TYPE_SHORT:
    			sb.append(String.valueOf(bindValue.shortBinding));
    			break;
    		case Fields.FIELD_TYPE_LONG:
    			sb.append(String.valueOf(bindValue.intBinding));
    			break;
    		case Fields.FIELD_TYPE_LONGLONG:
    			sb.append(String.valueOf(bindValue.longBinding));
    			break;
    		case Fields.FIELD_TYPE_FLOAT:
    			sb.append(String.valueOf(bindValue.floatBinding));
    			break;
    		case Fields.FIELD_TYPE_DOUBLE:
    			sb.append(String.valueOf(bindValue.doubleBinding));
    			break;
    		case Fields.FIELD_TYPE_VAR_STRING:
            case Fields.FIELD_TYPE_STRING:
            case Fields.FIELD_TYPE_VARCHAR:
            	bindValue.value = varcharEscaper.asFunction().apply(String.valueOf(bindValue.value));
            	sb.append("'" + bindValue.value + "'");
            	break;
            case Fields.FIELD_TYPE_TINY_BLOB:
            case Fields.FIELD_TYPE_BLOB:
            case Fields.FIELD_TYPE_MEDIUM_BLOB:
            case Fields.FIELD_TYPE_LONG_BLOB:
            	if(bindValue.value instanceof ByteArrayOutputStream) {
            		byte[] bytes = ((ByteArrayOutputStream) bindValue.value).toByteArray();
            		sb.append("X'" + HexFormatUtil.bytesToHexString(bytes) + "'");
            	} else {
            		// 正常情况下不会走到else, 除非long data的存储方式(ByteArrayOutputStream)被修改
            		LOGGER.warn("bind value is not a instance of ByteArrayOutputStream, maybe someone change the implement of long data storage!");
            		sb.append("'" + bindValue.value + "'");
            	}
            	break;
            case Fields.FIELD_TYPE_TIME:
            case Fields.FIELD_TYPE_DATE:
            case Fields.FIELD_TYPE_DATETIME:
            case Fields.FIELD_TYPE_TIMESTAMP:
            	sb.append("'" + bindValue.value + "'");
            	break;
            default:
            	bindValue.value = varcharEscaper.asFunction().apply(String.valueOf(bindValue.value));
            	sb.append(bindValue.value.toString());
            	break;
    		}
    	}
    	
    	return sb.toString();
    }

}