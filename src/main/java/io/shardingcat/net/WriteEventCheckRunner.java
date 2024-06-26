package io.shardingcat.net;

public class WriteEventCheckRunner implements Runnable {
	private final SocketWR socketWR;
	private volatile boolean finshed = true;

	public WriteEventCheckRunner(SocketWR socketWR) {
		this.socketWR = socketWR;
	}

	public boolean isFinished() {
		return finshed;
	}

	@Override
	public void run() {
		finshed = false;
		socketWR.doNextWriteCheck();
		finshed = true;
	}
}