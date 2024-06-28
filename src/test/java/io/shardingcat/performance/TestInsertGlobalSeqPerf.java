
package io.shardingcat.performance;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wuzh
 * 
 */
public class TestInsertGlobalSeqPerf extends AbstractMultiTreadBatchTester {

	public static void main(String[] args) throws Exception {
       new TestInsertGlobalSeqPerf().run(args);
       

	}

	@Override
	public Runnable createJob(SimpleConPool conPool2, long myCount, int batch,
			long startId, AtomicLong finshiedCount2,
			AtomicLong failedCount2) {
		  return new TravelRecordGlobalSeqInsertJob(conPool2,
					myCount, batch, startId, finshiedCount, failedCount);
	}

	

	



	
}