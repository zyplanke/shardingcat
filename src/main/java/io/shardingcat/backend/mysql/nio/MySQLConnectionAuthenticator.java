
package io.shardingcat.backend.mysql.nio;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.mysql.CharsetUtil;
import io.shardingcat.backend.mysql.SecurityUtil;
import io.shardingcat.backend.mysql.nio.handler.ResponseHandler;
import io.shardingcat.config.Capabilities;
import io.shardingcat.net.ConnectionException;
import io.shardingcat.net.NIOHandler;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.ErrorPacket;
import io.shardingcat.net.mysql.HandshakePacket;
import io.shardingcat.net.mysql.OkPacket;
import io.shardingcat.net.mysql.Reply323Packet;

/**
 * MySQL 验证处理器
 * 
 * @author shardingcat
 */
public class MySQLConnectionAuthenticator implements NIOHandler {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MySQLConnectionAuthenticator.class);
	private final MySQLConnection source;
	private final ResponseHandler listener;

	public MySQLConnectionAuthenticator(MySQLConnection source,
			ResponseHandler listener) {
		this.source = source;
		this.listener = listener;
	}

	public void connectionError(MySQLConnection source, Throwable e) {
		listener.connectionError(e, source);
	}

	@Override
	public void handle(byte[] data) {
		try {
			switch (data[4]) {
			case OkPacket.FIELD_COUNT:
				HandshakePacket packet = source.getHandshake();
				if (packet == null) {
					processHandShakePacket(data);
					// 发送认证数据包
					source.authenticate();
					break;
				}
				// 处理认证结果
				source.setHandler(new MySQLConnectionHandler(source));
				source.setAuthenticated(true);
				boolean clientCompress = Capabilities.CLIENT_COMPRESS==(Capabilities.CLIENT_COMPRESS & packet.serverCapabilities);
				boolean usingCompress= ShardingCatServer.getInstance().getConfig().getSystem().getUseCompression()==1 ;
				if(clientCompress&&usingCompress)
				{
					source.setSupportCompress(true);
				}
				if (listener != null) {
					listener.connectionAcquired(source);
				}
				break;
			case ErrorPacket.FIELD_COUNT:
				ErrorPacket err = new ErrorPacket();
				err.read(data);
				String errMsg = new String(err.message);
				LOGGER.warn("can't connect to mysql server ,errmsg:"+errMsg+" "+source);
				//source.close(errMsg);
				throw new ConnectionException(err.errno, errMsg);

			case EOFPacket.FIELD_COUNT:
				auth323(data[3]);
				break;
			default:
				packet = source.getHandshake();
				if (packet == null) {
					processHandShakePacket(data);
					// 发送认证数据包
					source.authenticate();
					break;
				} else {
					throw new RuntimeException("Unknown Packet!");
				}

			}

		} catch (RuntimeException e) {
			if (listener != null) {
				listener.connectionError(e, source);
				return;
			}
			throw e;
		}
	}

	private void processHandShakePacket(byte[] data) {
		// 设置握手数据包
		HandshakePacket packet= new HandshakePacket();
		packet.read(data);
		source.setHandshake(packet);
		source.setThreadId(packet.threadId);

		// 设置字符集编码
		int charsetIndex = (packet.serverCharsetIndex & 0xff);
		String charset = CharsetUtil.getCharset(charsetIndex);
		if (charset != null) {
			source.setCharset(charset);
		} else {
			throw new RuntimeException("Unknown charsetIndex:" + charsetIndex);
		}
	}

	private void auth323(byte packetId) {
		// 发送323响应认证数据包
		Reply323Packet r323 = new Reply323Packet();
		r323.packetId = ++packetId;
		String pass = source.getPassword();
		if (pass != null && pass.length() > 0) {
			byte[] seed = source.getHandshake().seed;
			r323.seed = SecurityUtil.scramble323(pass, new String(seed))
					.getBytes();
		}
		r323.write(source);
	}

}