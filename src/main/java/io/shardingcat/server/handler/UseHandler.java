
package io.shardingcat.server.handler;

import java.nio.ByteBuffer;
import java.util.Set;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.net.handler.FrontendPrivileges;
import io.shardingcat.net.mysql.OkPacket;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.util.StringUtil;

/**
 * @author shardingcat
 */
public final class UseHandler {

    public static void handle(String sql, ServerConnection c, int offset) {
        String schema = sql.substring(offset).trim();
        int length = schema.length();
        if (length > 0) {
        	if(schema.endsWith(";")) {
                schema = schema.substring(0, schema.length() - 1);
            }
        	schema = StringUtil.replaceChars(schema, "`", null);
        	length=schema.length();
            if (schema.charAt(0) == '\'' && schema.charAt(length - 1) == '\'') {
                schema = schema.substring(1, length - 1);
            }
        }
        // 检查schema的有效性
        FrontendPrivileges privileges = c.getPrivileges();
        if (schema == null || !privileges.schemaExists(schema)) {
            c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + schema + "'");
            return;
        }
        String user = c.getUser();
        if (!privileges.userExists(user, c.getHost())) {
            c.writeErrMessage(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + c.getUser() + "'");
            return;
        }
        Set<String> schemas = privileges.getUserSchemas(user);
        if (schemas == null || schemas.size() == 0 || schemas.contains(schema)) {
            c.setSchema(schema);
            ByteBuffer buffer = c.allocate();
            c.write(c.writeToBuffer(OkPacket.OK, buffer));
        } else {
            String msg = "Access denied for user '" + c.getUser() + "' to database '" + schema + "'";
            c.writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, msg);
        }
    }

}