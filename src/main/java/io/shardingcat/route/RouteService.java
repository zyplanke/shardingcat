
package io.shardingcat.route;

import java.sql.SQLNonTransientException;
import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.shardingcat.cache.CachePool;
import io.shardingcat.cache.CacheService;
import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.route.factory.RouteStrategyFactory;
import io.shardingcat.route.function.PartitionByCRC32PreSlot;
import io.shardingcat.route.handler.HintHandler;
import io.shardingcat.route.handler.HintHandlerFactory;
import io.shardingcat.route.handler.HintSQLHandler;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.server.parser.ServerParse;

public class RouteService {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RouteService.class);
    public static final String SHARDINGCAT_HINT_TYPE = "_shardingcatHintType";
    private final CachePool sqlRouteCache;
	private final LayerCachePool tableId2DataNodeCache;	

	private final String OLD_SHARDINGCAT_HINT = "/*!shardingcat:"; 	// 处理自定义分片注解, 注解格式：/*!shardingcat: type = value */ sql
	private final String NEW_SHARDINGCAT_HINT = "/*#shardingcat:"; 	// 新的注解格式:/* !shardingcat: type = value */ sql，oldShardingCatHint的格式不兼容直连mysql
    private final String HINT_SPLIT = "=";

	public RouteService(CacheService cachService) {
		sqlRouteCache = cachService.getCachePool("SQLRouteCache");
		tableId2DataNodeCache = (LayerCachePool) cachService
				.getCachePool("TableID2DataNodeCache");
	}

	public LayerCachePool getTableId2DataNodeCache() {
		return tableId2DataNodeCache;
	}

	public RouteResultset route(SystemConfig sysconf, SchemaConfig schema,
			int sqlType, String stmt, String charset, ServerConnection sc)
			throws SQLNonTransientException {
		RouteResultset rrs = null;
		String cacheKey = null;

		/**
		 *  SELECT 类型的SQL, 检测
		 */
		if (sqlType == ServerParse.SELECT) {
			cacheKey = schema.getName() + stmt;			
			rrs = (RouteResultset) sqlRouteCache.get(cacheKey);
			if (rrs != null) {
				checkMigrateRule(schema.getName(),rrs,sqlType);
				return rrs;
			}
		}
        
		/*!shardingcat: sql = select name from aa */
        /*!shardingcat: schema = test */
//      boolean isMatchOldHint = stmt.startsWith(OLD_SHARDINGCAT_HINT);
//      boolean isMatchNewHint = stmt.startsWith(NEW_SHARDINGCAT_HINT);
//		if (isMatchOldHint || isMatchNewHint ) {
		int hintLength = RouteService.isHintSql(stmt);
		if(hintLength != -1){
			int endPos = stmt.indexOf("*/");
			if (endPos > 0) {				
				// 用!shardingcat:内部的语句来做路由分析
//				int hintLength = isMatchOldHint ? OLD_SHARDINGCAT_HINT.length() : NEW_SHARDINGCAT_HINT.length();
				String hint = stmt.substring(hintLength, endPos).trim();	
				
                int firstSplitPos = hint.indexOf(HINT_SPLIT);                
                if(firstSplitPos > 0 ){
                    Map hintMap=    parseHint(hint);
                	String hintType = (String) hintMap.get(SHARDINGCAT_HINT_TYPE);
                    String hintSql = (String) hintMap.get(hintType);
                    if( hintSql.length() == 0 ) {
                    	LOGGER.warn("comment int sql must meet :/*!shardingcat:type=value*/ or /*#shardingcat:type=value*/ or /*shardingcat:type=value*/: "+stmt);
                    	throw new SQLSyntaxErrorException("comment int sql must meet :/*!shardingcat:type=value*/ or /*#shardingcat:type=value*/ or /*shardingcat:type=value*/: "+stmt);
                    }
                    String realSQL = stmt.substring(endPos + "*/".length()).trim();

                    HintHandler hintHandler = HintHandlerFactory.getHintHandler(hintType);
                    if( hintHandler != null ) {    

                    	if ( hintHandler instanceof  HintSQLHandler) {                    		
                          	/**
                        	 * 修复 注解SQL的 sqlType 与 实际SQL的 sqlType 不一致问题， 如： hint=SELECT，real=INSERT
                        	 * fixed by zhuam
                        	 */
                    		int hintSqlType = ServerParse.parse( hintSql ) & 0xff;     
                    		rrs = hintHandler.route(sysconf, schema, sqlType, realSQL, charset, sc, tableId2DataNodeCache, hintSql,hintSqlType,hintMap);
                    		
                    	} else {                    		
                    		rrs = hintHandler.route(sysconf, schema, sqlType, realSQL, charset, sc, tableId2DataNodeCache, hintSql,sqlType,hintMap);
                    	}
 
                    }else{
                        LOGGER.warn("TODO , support hint sql type : " + hintType);
                    }
                    
                }else{//fixed by runfriends@126.com
                	LOGGER.warn("comment in sql must meet :/*!shardingcat:type=value*/ or /*#shardingcat:type=value*/ or /*shardingcat:type=value*/: "+stmt);
                	throw new SQLSyntaxErrorException("comment in sql must meet :/*!mcat:type=value*/ or /*#shardingcat:type=value*/ or /*shardingcat:type=value*/: "+stmt);
                }
			}
		} else {
			stmt = stmt.trim();
			rrs = RouteStrategyFactory.getRouteStrategy().route(sysconf, schema, sqlType, stmt,
					charset, sc, tableId2DataNodeCache);
		}

		if (rrs != null && sqlType == ServerParse.SELECT && rrs.isCacheAble()) {
			sqlRouteCache.putIfAbsent(cacheKey, rrs);
		}
		checkMigrateRule(schema.getName(),rrs,sqlType);
		return rrs;
	}

	 //数据迁移的切换准备阶段，需要拒绝写操作和所有的跨多节点写操作
		private   void checkMigrateRule(String schemal,RouteResultset rrs,int sqlType ) throws SQLNonTransientException {
		if(rrs!=null&&rrs.getTables()!=null){
			boolean isUpdate=isUpdateSql(sqlType);
			if(!isUpdate)return;
			ConcurrentMap<String,List<PartitionByCRC32PreSlot.Range>> tableRules= RouteCheckRule.migrateRuleMap.get(schemal.toUpperCase()) ;
			if(tableRules!=null){
			for (String table : rrs.getTables()) {
				List<PartitionByCRC32PreSlot.Range> rangeList= tableRules.get(table.toUpperCase()) ;
				if(rangeList!=null&&!rangeList.isEmpty()){
					if(rrs.getNodes().length>1&&isUpdate){
						throw new   SQLNonTransientException ("schema:"+schemal+",table:"+table+",sql:"+rrs.getStatement()+" is not allowed,because table is migrate switching,please wait for a moment");
					}
					for (PartitionByCRC32PreSlot.Range range : rangeList) {
						RouteResultsetNode[] routeResultsetNodes=	rrs.getNodes();
						for (RouteResultsetNode routeResultsetNode : routeResultsetNodes) {
							int slot=routeResultsetNode.getSlot();
							if(isUpdate&&slot>=range.start&&slot<=range.end){
								throw new   SQLNonTransientException ("schema:"+schemal+",table:"+table+",sql:"+rrs.getStatement()+" is not allowed,because table is migrate switching,please wait for a moment");

							}
						}
					}
				}
			}
			}
		}
	}


	private boolean isUpdateSql(int type) {
		return ServerParse.INSERT==type||ServerParse.UPDATE==type||ServerParse.DELETE==type||ServerParse.DDL==type;
	}

	public static int isHintSql(String sql){
		int j = 0;
		int len = sql.length();
		if(sql.charAt(j++) == '/' && sql.charAt(j++) == '*'){
			char c = sql.charAt(j);
			// 过滤掉 空格 和 * 两种字符, 支持： "/** !shardingcat: */" 和 "/** #shardingcat: */" 形式的注解
			while(j < len && c != '!' && c != '#' && (c == ' ' || c == '*')){
				c = sql.charAt(++j);
			}
			//注解支持的'!'不被mysql单库兼容，
			//注解支持的'#'不被mybatis兼容
			//注解支持的':'不被hibernate兼容
			//考虑用shardingcat字符前缀标志Hintsql:"/** shardingcat: */"
			if(sql.charAt(j)=='m'){
				j--;
			}
			if(j + 6 >= len)	{// prevent the following sql.charAt overflow
				return -1;        // false
			}
			if(sql.charAt(++j) == 'm' && sql.charAt(++j) == 'y' && sql.charAt(++j) == 'c'
				&& sql.charAt(++j) == 'a' && sql.charAt(++j) == 't' && (sql.charAt(++j) == ':' || sql.charAt(j) == '#')) {
				return j + 1;    // true，同时返回注解部分的长度
			}
		}
		return -1;	// false
	}
	
	 private   Map parseHint( String sql)
    {
        Map map=new HashMap();
        int y=0;
        int begin=0;
        for(int i=0;i<sql.length();i++)
        {
            char cur=sql.charAt(i);
            if(cur==','&& y%2==0)
            {
                String substring = sql.substring(begin, i);

                parseKeyValue(map, substring);
                begin=i+1;
            }
            else
            if(cur=='\'')
            {
                y++;
            } if(i==sql.length()-1)
        {
            parseKeyValue(map, sql.substring(begin));

        }


        }
        return map;
    }

    private  void parseKeyValue(Map map, String substring)
    {
        int indexOf = substring.indexOf('=');
        if(indexOf!=-1)
        {

            String key=substring.substring(0,indexOf).trim().toLowerCase();
            String value=substring.substring(indexOf+1,substring.length());
            if(value.endsWith("'")&&value.startsWith("'"))
            {
                value=value.substring(1,value.length()-1);
            }
            if(map.isEmpty())
            {
              map.put(SHARDINGCAT_HINT_TYPE,key)  ;
            }
            map.put(key,value.trim());

        }
    }
}
