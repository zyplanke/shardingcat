

package io.shardingcat.memory.unsafe.memory;


import io.shardingcat.memory.unsafe.memory.mm.MemoryManager;
import io.shardingcat.memory.unsafe.memory.mm.MemoryMode;
import io.shardingcat.memory.unsafe.utils.ShardingCatPropertyConf;

public  class TestMemoryManager extends MemoryManager {

  public  TestMemoryManager(ShardingCatPropertyConf conf){
          super(conf,1, Long.MAX_VALUE);
  }

  private boolean oomOnce = false;
  private long available = Long.MAX_VALUE;



    @Override
   protected long  acquireExecutionMemory(
     long numBytes,
     long taskAttemptId,
     MemoryMode memoryMode){
    if (oomOnce) {
      oomOnce = false;
      return 0;
    } else if (available >= numBytes) {
      available -= numBytes;
     return numBytes;
    } else {
      long grant = available;
      available = 0;
     return grant;
    }
  }

@Override
public void releaseExecutionMemory(
        long numBytes,
        long taskAttemptId,
        MemoryMode memoryMode){
    available += numBytes;
  }


  public void markExecutionAsOutOfMemoryOnce(){
    oomOnce = true;
  }

  public void limit(long avail){
    available = avail;
  }

}
