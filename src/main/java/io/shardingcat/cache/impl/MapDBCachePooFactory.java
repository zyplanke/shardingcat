
package io.shardingcat.cache.impl;

import java.util.concurrent.TimeUnit;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import io.shardingcat.cache.CachePool;
import io.shardingcat.cache.CachePoolFactory;

public class MapDBCachePooFactory extends CachePoolFactory {
	private DB db = DBMaker.newMemoryDirectDB().cacheSize(1000).cacheLRUEnable().make();

	@Override
	public CachePool createCachePool(String poolName, int cacheSize,
			int expiredSeconds) {

		HTreeMap<Object, Object> cache = this.db.createHashMap(poolName)
				.expireMaxSize(cacheSize)
				.expireAfterAccess(expiredSeconds, TimeUnit.SECONDS)
				.makeOrGet();
		return new MapDBCachePool(cache, cacheSize);

	}

}