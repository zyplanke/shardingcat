package io.shardingcat.memory.unsafe.storage;



import io.shardingcat.memory.unsafe.utils.ShardingCatPropertyConf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zagnix on 2016/6/3.
 */
public class DataNodeDiskManager {

    private ShardingCatPropertyConf conf;
    private  boolean deleteFilesOnStop;
    private  SerializerManager serializerManager;

    public DataNodeDiskManager(ShardingCatPropertyConf conf, boolean deleteFilesOnStop, SerializerManager  serializerManager){
        this.conf = conf;
        this.deleteFilesOnStop = deleteFilesOnStop;
        this.serializerManager = serializerManager;
    }

    public DataNodeFileManager diskBlockManager() throws IOException {
        return new DataNodeFileManager(conf, deleteFilesOnStop);
    }


    /**
     * A short circuited method to get a block writer that can write data directly to disk.
     * The Block will be appended to the File specified by filename. Callers should handle error
     * cases.
     */
    public DiskRowWriter getDiskWriter(
            ConnectionId blockId,
            File file,
            SerializerInstance serializerInstance,
            int bufferSize) throws IOException {
        boolean syncWrites = conf.getBoolean("shardingcat.merge.sync", false);
        return new DiskRowWriter(file, serializerInstance, bufferSize,new FileOutputStream(file),
                syncWrites,blockId);
    }
}
