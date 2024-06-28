
package io.shardingcat.config;

/**
 * ShardingCat报警关键词定义
 * 
 * @author shardingcat
 */
public interface Alarms {
    /** 默认报警关键词 **/
    public static final String DEFAULT           = "#!ShardingCat#";
    
    /** 集群无有效的节点可提供服务 **/
    public static final String CLUSTER_EMPTY     = "#!CLUSTER_EMPTY#";
    
    /** 数据节点的数据源发生切换 **/
    public static final String DATANODE_SWITCH   = "#!DN_SWITCH#";
    
    /** 防火墙非法用户访问 **/
    public static final String FIREWALL_ATTACK = "#!QT_ATTACK#";
   
    /** 非法DML **/ 
    public static final String DML_ATTACK = "#!DML_ATTACK#";
    
}
