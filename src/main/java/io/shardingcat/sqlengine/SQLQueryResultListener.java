package io.shardingcat.sqlengine;


public interface SQLQueryResultListener<T> {

	public void onResult(T result);

}
