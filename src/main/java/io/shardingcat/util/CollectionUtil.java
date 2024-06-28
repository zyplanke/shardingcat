
package io.shardingcat.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author shardingcat
 */
public class CollectionUtil {
    /**
     * @param orig
     *            if null, return intersect
     */
    public static Set<? extends Object> intersectSet(Set<? extends Object> orig, Set<? extends Object> intersect) {
        if (orig == null) {
            return intersect;
        }
        if (intersect == null || orig.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Object> set = new HashSet<Object>(orig.size());
        for (Object p : orig) {
            if (intersect.contains(p)) {
                set.add(p);
            }
        }
        return set;
    }
    public static boolean isEmpty(Collection<?> collection){
    	return collection==null || collection.isEmpty();
    }
    public static boolean isEmpty(Map<?,?> map){
    	return map==null || map.isEmpty();
    }
}