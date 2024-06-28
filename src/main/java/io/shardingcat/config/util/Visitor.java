
package io.shardingcat.config.util;

/**
 * @author shardingcat
 */
public interface Visitor {

    void visit(String name, Class<?> type, Class<?> definedIn, Object value);

}