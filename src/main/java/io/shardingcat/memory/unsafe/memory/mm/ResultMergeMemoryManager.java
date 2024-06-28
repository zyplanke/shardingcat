package io.shardingcat.memory.unsafe.memory.mm;


import io.shardingcat.memory.unsafe.utils.ShardingCatPropertyConf;

/**
 * Created by zagnix on 2016/6/7.
 */
public class ResultMergeMemoryManager extends MemoryManager {

    private long  maxOnHeapExecutionMemory;
    private int numCores;
    private ShardingCatPropertyConf conf;
    public ResultMergeMemoryManager(ShardingCatPropertyConf conf, int numCores, long onHeapExecutionMemory){
        super(conf,numCores,onHeapExecutionMemory);
        this.conf = conf;
        this.numCores = numCores;
        this.maxOnHeapExecutionMemory = onHeapExecutionMemory;
    }

    @Override
    protected  synchronized long acquireExecutionMemory(long numBytes,long taskAttemptId,MemoryMode memoryMode) throws InterruptedException {
        switch (memoryMode) {
            case ON_HEAP:
                return  onHeapExecutionMemoryPool.acquireMemory(numBytes,taskAttemptId);
            case OFF_HEAP:
                return  offHeapExecutionMemoryPool.acquireMemory(numBytes,taskAttemptId);
        }
        return 0L;
    }

}
