
package io.shardingcat.model;

/**
 * @author shardingcat
 */
public class TransferObject {
    long handleCount;
    long compeleteCount;

    public void handle() {
        handleCount++;
    }

    public void compelete() {
        compeleteCount++;
    }

}