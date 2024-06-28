
package io.shardingcat.manager;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.manager.handler.*;
import io.shardingcat.manager.response.KillConnection;
import io.shardingcat.manager.response.Offline;
import io.shardingcat.manager.response.Online;
import io.shardingcat.net.handler.FrontendQueryHandler;
import io.shardingcat.net.mysql.OkPacket;
import io.shardingcat.route.parser.ManagerParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shardingcat
 */
public class ManagerQueryHandler implements FrontendQueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerQueryHandler.class);
    private static final int SHIFT = 8;
    private final ManagerConnection source;
    protected Boolean readOnly;

    public ManagerQueryHandler(ManagerConnection source) {
        this.source = source;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public void query(String sql) {
        ManagerConnection c = this.source;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append(c).append(sql).toString());
        }
        int rs = ManagerParse.parse(sql);
        switch (rs & 0xff) {
            case ManagerParse.SELECT:
                SelectHandler.handle(sql, c, rs >>> SHIFT);
                break;
            case ManagerParse.SET:
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                break;
            case ManagerParse.SHOW:
                ShowHandler.handle(sql, c, rs >>> SHIFT);
                break;
            case ManagerParse.SWITCH:
                SwitchHandler.handler(sql, c, rs >>> SHIFT);
                break;
            case ManagerParse.KILL_CONN:
                KillConnection.response(sql, rs >>> SHIFT, c);
                break;
            case ManagerParse.OFFLINE:
                Offline.execute(sql, c);
                break;
            case ManagerParse.ONLINE:
                Online.execute(sql, c);
                break;
            case ManagerParse.STOP:
                StopHandler.handle(sql, c, rs >>> SHIFT);
                break;
            case ManagerParse.RELOAD:
                ReloadHandler.handle(sql, c, rs >>> SHIFT);
                break;
            case ManagerParse.ROLLBACK:
                RollbackHandler.handle(sql, c, rs >>> SHIFT);
                break;
            case ManagerParse.CLEAR:
                ClearHandler.handle(sql, c, rs >>> SHIFT);
                break;
            case ManagerParse.CONFIGFILE:
                ConfFileHandler.handle(sql, c);
                break;
            case ManagerParse.LOGFILE:
                ShowServerLog.handle(sql, c);
                break;
            case ManagerParse.ZK:
                ZKHandler.handle(sql, c, rs >>> SHIFT);
                break;
            default:
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}