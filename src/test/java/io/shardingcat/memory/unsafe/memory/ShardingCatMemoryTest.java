package io.shardingcat.memory.unsafe.memory;


import io.shardingcat.memory.ShardingCatMemory;
import io.shardingcat.memory.unsafe.Platform;
import org.junit.Test;

/**
 * Created by zagnix on 2016/6/12.
 */
public class ShardingCatMemoryTest {

    /**
     * -Xmx1024m -XX:MaxDirectMemorySize=1G
     */
    @Test
    public void testShardingCatMemory() throws NoSuchFieldException, IllegalAccessException {
        ShardingCatMemory shardingCatMemory = new ShardingCatMemory();
        System.out.println(shardingCatMemory.getResultSetBufferSize());
        System.out.println(Platform.getMaxHeapMemory());
        System.out.println(Platform.getMaxDirectMemory());
    }

}
