package io.shardingcat.sqlengine;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import io.shardingcat.manager.handler.ConfFileHandler;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.ResultSetHeaderPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.server.NonBlockingSession;
import io.shardingcat.server.ServerConnection;

public class EngineCtx {
	public static final Logger LOGGER = LoggerFactory.getLogger(ConfFileHandler.class);
	private final BatchSQLJob bachJob;
	private AtomicInteger jobId = new AtomicInteger(0);
	AtomicInteger packetId = new AtomicInteger(0);
	private final NonBlockingSession session;
	private AtomicBoolean finished = new AtomicBoolean(false);
	private AllJobFinishedListener allJobFinishedListener;
	private AtomicBoolean headerWrited = new AtomicBoolean();
	private final ReentrantLock writeLock = new ReentrantLock();
	private volatile boolean hasError = false;

	public EngineCtx(NonBlockingSession session) {
		this.bachJob = new BatchSQLJob();
		this.session = session;
	}

	public byte incPackageId() {
		return (byte) packetId.incrementAndGet();
	}

	public void executeNativeSQLSequnceJob(String[] dataNodes, String sql,
			SQLJobHandler jobHandler) {
		for (String dataNode : dataNodes) {
			SQLJob job = new SQLJob(jobId.incrementAndGet(), sql, dataNode,
					jobHandler, this);
			bachJob.addJob(job, false);

		}
	}

	public ReentrantLock getWriteLock() {
		return writeLock;
	}

	public void setAllJobFinishedListener(
			AllJobFinishedListener allJobFinishedListener) {
		this.allJobFinishedListener = allJobFinishedListener;
	}

	public void executeNativeSQLParallJob(String[] dataNodes, String sql,
			SQLJobHandler jobHandler) {
		for (String dataNode : dataNodes) {
			SQLJob job = new SQLJob(jobId.incrementAndGet(), sql, dataNode,
					jobHandler, this);
			bachJob.addJob(job, true);

		}
	}

	/**
	 * set no more jobs created
	 */
	public void endJobInput() {
		bachJob.setNoMoreJobInput(true);
	}

	public void writeHeader(List<byte[]> afields, List<byte[]> bfields) {
		if (headerWrited.compareAndSet(false, true)) {
			try {
				writeLock.lock();
				// write new header
				ResultSetHeaderPacket headerPkg = new ResultSetHeaderPacket();
				headerPkg.fieldCount = afields.size() +bfields.size()-1;
				headerPkg.packetId = incPackageId();
				LOGGER.debug("packge id " + headerPkg.packetId);
				ServerConnection sc = session.getSource();
				ByteBuffer buf = headerPkg.write(sc.allocate(), sc, true);
				// wirte a fields
				for (byte[] field : afields) {
					field[3] = incPackageId();
					buf = sc.writeToBuffer(field, buf);
				}
				// write b field
				for (int i=1;i<bfields.size();i++) {
				  byte[] bfield = bfields.get(i);
				  bfield[3] = incPackageId();
				  buf = sc.writeToBuffer(bfield, buf);
				}
				// write field eof
				EOFPacket eofPckg = new EOFPacket();
				eofPckg.packetId = incPackageId();
				buf = eofPckg.write(buf, sc, true);
				sc.write(buf);
				//LOGGER.info("header outputed ,packgId:" + eofPckg.packetId);
			} finally {
				writeLock.unlock();
			}
		}

	}
	
	public void writeHeader(List<byte[]> afields) {
		if (headerWrited.compareAndSet(false, true)) {
			try {
				writeLock.lock();
				// write new header
				ResultSetHeaderPacket headerPkg = new ResultSetHeaderPacket();
				headerPkg.fieldCount = afields.size();// -1;
				headerPkg.packetId = incPackageId();
				LOGGER.debug("packge id " + headerPkg.packetId);
				ServerConnection sc = session.getSource();
				ByteBuffer buf = headerPkg.write(sc.allocate(), sc, true);
				// wirte a fields
				for (byte[] field : afields) {
					field[3] = incPackageId();
					buf = sc.writeToBuffer(field, buf);
				}

				// write field eof
				EOFPacket eofPckg = new EOFPacket();
				eofPckg.packetId = incPackageId();
				buf = eofPckg.write(buf, sc, true);
				sc.write(buf);
				//LOGGER.info("header outputed ,packgId:" + eofPckg.packetId);
			} finally {
				writeLock.unlock();
			}
		}

	}
	
	public void writeRow(RowDataPacket rowDataPkg) {
		ServerConnection sc = session.getSource();
		try {
			writeLock.lock();
			rowDataPkg.packetId = incPackageId();
			// 输出完整的 记录到客户端
			ByteBuffer buf = rowDataPkg.write(sc.allocate(), sc, true);
			sc.write(buf);
			//LOGGER.info("write  row ,packgId:" + rowDataPkg.packetId);
		} finally {
			writeLock.unlock();
		}
	}

	public void writeEof() {
		ServerConnection sc = session.getSource();
		EOFPacket eofPckg = new EOFPacket();
		eofPckg.packetId = incPackageId();
		ByteBuffer buf = eofPckg.write(sc.allocate(), sc, false);
		sc.write(buf);
		LOGGER.info("write  eof ,packgId:" + eofPckg.packetId);
	}
	

	public NonBlockingSession getSession() {
		return session;
	}

	public void onJobFinished(SQLJob sqlJob) {

		boolean allFinished = bachJob.jobFinished(sqlJob);
		if (allFinished && finished.compareAndSet(false, true)) {
			if(!hasError){
				LOGGER.info("all job finished  for front connection: "
						+ session.getSource());
				allJobFinishedListener.onAllJobFinished(this);
			}else{
				LOGGER.info("all job finished with error for front connection: "
						+ session.getSource());
			}
		}

	}

	public boolean isHasError() {
		return hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

}
