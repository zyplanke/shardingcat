
package io.shardingcat.manager.handler;

import static io.shardingcat.route.parser.ManagerParseSelect.SESSION_AUTO_INCREMENT;
import static io.shardingcat.route.parser.ManagerParseSelect.VERSION_COMMENT;
import static io.shardingcat.route.parser.ManagerParseSelect.SESSION_TX_READ_ONLY;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.manager.response.SelectSessionAutoIncrement;
import io.shardingcat.manager.response.SelectSessionTxReadOnly;
import io.shardingcat.manager.response.SelectVersionComment;
import io.shardingcat.route.parser.ManagerParseSelect;

/**
 * @author shardingcat
 */
public final class SelectHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseSelect.parse(stmt, offset)) {
        case VERSION_COMMENT:
            SelectVersionComment.execute(c);
            break;
        case SESSION_AUTO_INCREMENT:
            SelectSessionAutoIncrement.execute(c);
            break;
        case SESSION_TX_READ_ONLY:
        	SelectSessionTxReadOnly.execute(c);
        	break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}