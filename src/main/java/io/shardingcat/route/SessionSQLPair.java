package io.shardingcat.route;

import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.server.NonBlockingSession;

public class SessionSQLPair {
	public final NonBlockingSession session;
	
	public final SchemaConfig schema;
	public final String sql;
	public final int type;

	public SessionSQLPair(NonBlockingSession session, SchemaConfig schema,
			String sql,int type) {
		super();
		this.session = session;
		this.schema = schema;
		this.sql = sql;
		this.type=type;
	}

}
