
package io.shardingcat.config;

/**
 * 事务隔离级别定义
 * 
 * @author shardingcat
 */
public interface Isolations {

    public static final int READ_UNCOMMITTED = 1;
    public static final int READ_COMMITTED = 2;
    public static final int REPEATED_READ = 3;
    public static final int SERIALIZABLE = 4;

}