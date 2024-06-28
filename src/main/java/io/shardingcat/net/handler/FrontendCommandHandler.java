
package io.shardingcat.net.handler;

import io.shardingcat.backend.mysql.MySQLMessage;
import io.shardingcat.config.ErrorCode;
import io.shardingcat.net.FrontendConnection;
import io.shardingcat.net.NIOHandler;
import io.shardingcat.net.mysql.MySQLPacket;
import io.shardingcat.statistic.CommandCount;

/**
 * 前端命令处理器
 *
 * @author shardingcat
 */
public class FrontendCommandHandler implements NIOHandler
{

    protected final FrontendConnection source;
    protected final CommandCount commands;

    public FrontendCommandHandler(FrontendConnection source)
    {
        this.source = source;
        this.commands = source.getProcessor().getCommands();
    }

    @Override
    public void handle(byte[] data)
    {
        if(source.getLoadDataInfileHandler()!=null&&source.getLoadDataInfileHandler().isStartLoadData())
        {
            MySQLMessage mm = new MySQLMessage(data);
            int  packetLength = mm.readUB3();
            if(packetLength+4==data.length)
            {
                source.loadDataInfileData(data);
            }
            return;
        }
        switch (data[4])
        {
            case MySQLPacket.COM_INIT_DB:
                commands.doInitDB();
                source.initDB(data);
                break;
            case MySQLPacket.COM_QUERY:
                commands.doQuery();
                source.query(data);
                break;
            case MySQLPacket.COM_PING:
                commands.doPing();
                source.ping();
                break;
            case MySQLPacket.COM_QUIT:
                commands.doQuit();
                source.close("quit cmd");
                break;
            case MySQLPacket.COM_PROCESS_KILL:
                commands.doKill();
                source.kill(data);
                break;
            case MySQLPacket.COM_STMT_PREPARE:
                commands.doStmtPrepare();
                source.stmtPrepare(data);
                break;
            case MySQLPacket.COM_STMT_SEND_LONG_DATA:
            	commands.doStmtSendLongData();
            	source.stmtSendLongData(data);
            	break;
            case MySQLPacket.COM_STMT_RESET:
            	commands.doStmtReset();
            	source.stmtReset(data);
            	break;
            case MySQLPacket.COM_STMT_EXECUTE:
                commands.doStmtExecute();
                source.stmtExecute(data);
                break;
            case MySQLPacket.COM_STMT_CLOSE:
                commands.doStmtClose();
                source.stmtClose(data);
                break;
            case MySQLPacket.COM_HEARTBEAT:
                commands.doHeartbeat();
                source.heartbeat(data);
                break;
            default:
                     commands.doOther();
                     source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR,
                             "Unknown command");

        }
    }

}