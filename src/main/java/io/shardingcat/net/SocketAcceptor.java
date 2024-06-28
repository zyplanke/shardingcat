package io.shardingcat.net;

public interface SocketAcceptor {

	void start();

	String getName();

	int getPort();

}
