
package io.shardingcat.manager.handler;

import io.shardingcat.config.ErrorCode;
import io.shardingcat.manager.ManagerConnection;
import io.shardingcat.manager.response.ReloadConfig;
import io.shardingcat.manager.response.ReloadQueryCf;
import io.shardingcat.manager.response.ReloadSqlSlowTime;
import io.shardingcat.manager.response.ReloadUser;
import io.shardingcat.manager.response.ReloadUserStat;
import io.shardingcat.route.parser.ManagerParseReload;
import io.shardingcat.route.parser.util.ParseUtil;

/**
 * @author shardingcat
 */
public final class ReloadHandler
{

    public static void handle(String stmt, ManagerConnection c, int offset)
    {
        int rs = ManagerParseReload.parse(stmt, offset);
        switch (rs)
        {
            case ManagerParseReload.CONFIG:
                ReloadConfig.execute(c,false);
                break;
            case ManagerParseReload.CONFIG_ALL:
                ReloadConfig.execute(c,true);
                break;
            case ManagerParseReload.ROUTE:
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
                break;
            case ManagerParseReload.USER:
                ReloadUser.execute(c);
                break;
            case ManagerParseReload.USER_STAT:
                ReloadUserStat.execute(c);
                break;
            case ManagerParseReload.SQL_SLOW:
            	ReloadSqlSlowTime.execute(c, ParseUtil.getSQLId(stmt));
                break;           
            case ManagerParseReload.QUERY_CF:            	
            	String filted = ParseUtil.parseString(stmt) ;
            	ReloadQueryCf.execute(c, filted);
            	break;                
            default:
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}