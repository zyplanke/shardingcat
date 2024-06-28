
package io.shardingcat.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import io.shardingcat.config.loader.xml.XMLServerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlReplaceStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.wall.WallCheckResult;
import com.alibaba.druid.wall.WallProvider;

import io.shardingcat.ShardingCatServer;
import io.shardingcat.config.model.FirewallConfig;
import io.shardingcat.config.model.UserConfig;
import io.shardingcat.config.model.UserPrivilegesConfig;
import io.shardingcat.net.handler.FrontendPrivileges;
import io.shardingcat.route.parser.druid.ShardingCatSchemaStatVisitor;
import io.shardingcat.route.parser.druid.ShardingCatStatementParser;

/**
 * @author shardingcat
 */
public class ShardingCatPrivileges implements FrontendPrivileges {
	/**
	 * 无需每次建立连接都new实例。
	 */
	private static ShardingCatPrivileges instance = new ShardingCatPrivileges();
	
    private static final Logger ALARM = LoggerFactory.getLogger("alarm");
    
    private static boolean check = false;	
	private final static ThreadLocal<WallProvider> contextLocal = new ThreadLocal<WallProvider>();

    public static ShardingCatPrivileges instance() {
    	return instance;
    }
    
    private ShardingCatPrivileges() {
    	super();
    }
    
    @Override
    public boolean schemaExists(String schema) {
        ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
        return conf.getSchemas().containsKey(schema);
    }

    @Override
    public boolean userExists(String user, String host) {
    	//检查用户及白名单
    	return checkFirewallWhiteHostPolicy(user, host);
    }

    @Override
    public String getPassword(String user) {
        ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
        if (user != null && user.equals(conf.getSystem().getClusterHeartbeatUser())) {
            return conf.getSystem().getClusterHeartbeatPass();
        } else {
            UserConfig uc = conf.getUsers().get(user);
            if (uc != null) {
                return uc.getPassword();
            } else {
                return null;
            }
        }
    }

    @Override
    public Set<String> getUserSchemas(String user) {
        ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
        
        UserConfig uc = conf.getUsers().get(user);
        if (uc != null) {
            return uc.getSchemas();
        } else {
            return null;
        }
    
     }
    
    @Override
    public Boolean isReadOnly(String user) {
        ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
       
        UserConfig uc = conf.getUsers().get(user);
        if (uc != null) {
            return uc.isReadOnly();
        } else {
            return null;
        }
    }

	@Override
	public int getBenchmark(String user) {
		ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
        UserConfig uc = conf.getUsers().get(user);
        if (uc != null) {
            return uc.getBenchmark();
        } else {
            return 0;
        }
	}

	/**
	 * 防火墙白名单处理，根据防火墙配置，判断目前主机是否可以通过某用户登陆
	 * 白名单配置请参考：
	 * @see  XMLServerLoader
	 * @see  FirewallConfig
	 *
	 * @modification 修改增加网段白名单识别配置
	 * @date 2016/12/8
	 * @modifiedBy Hash Zhang
	 */
	@Override
	public boolean checkFirewallWhiteHostPolicy(String user, String host) {
		
		ShardingCatConfig shardingcatConfig = ShardingCatServer.getInstance().getConfig();
        FirewallConfig firewallConfig = shardingcatConfig.getFirewall();
        
        //防火墙 白名单处理
        boolean isPassed = false;
        
        Map<String, List<UserConfig>> whitehost = firewallConfig.getWhitehost();
        Map<Pattern, List<UserConfig>> whitehostMask = firewallConfig.getWhitehostMask();
        if ((whitehost == null || whitehost.size() == 0)&&(whitehostMask == null || whitehostMask.size() == 0)) {
        	Map<String, UserConfig> users = shardingcatConfig.getUsers();
        	isPassed = users.containsKey(user);
        	
        } else {
        	List<UserConfig> list = whitehost.get(host);
			Set<Pattern> patterns = whitehostMask.keySet();
			if(patterns != null && patterns.size() > 0){
				for(Pattern pattern : patterns) {
					if(pattern.matcher(host).find()){
						isPassed = true;
						break;
					}
				}
			}
			if (list != null) {
				for (UserConfig userConfig : list) {
					if (userConfig.getName().equals(user)) {
						isPassed = true;
						break;
					}
				}
			}        	
        }
        
        if ( !isPassed ) {
        	 ALARM.error(new StringBuilder().append(Alarms.FIREWALL_ATTACK).append("[host=").append(host)
                     .append(",user=").append(user).append(']').toString());
        	 return false;
        }        
        return true;
	}

	
	/**
	 * @see https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE-wallfilter
	 */
	@Override
	public boolean checkFirewallSQLPolicy(String user, String sql) {
		
		boolean isPassed = true;
		
		if( contextLocal.get() == null ){
			FirewallConfig firewallConfig = ShardingCatServer.getInstance().getConfig().getFirewall();
			if ( firewallConfig != null) {
				if ( firewallConfig.isCheck() ) {
					contextLocal.set(firewallConfig.getProvider());
					check = true;
				}
			}
		}
		
		if( check ){
			WallCheckResult result = contextLocal.get().check(sql);
			
			// 修复 druid 防火墙在处理SHOW FULL TABLES WHERE Table_type != 'VIEW' 的时候存在的 BUG
			// 此代码有问题，由于Druid WallCheck 对同一条SQL语句只做一次解析，下面代码会导致第二次拦截失效
			// 并且 目前已经提供 ShowFullTables 来处理show full tables 命令，故对代码进行修改 
//			List<SQLStatement> stmts =  result.getStatementList();
//			if ( !stmts.isEmpty() &&  !( stmts.get(0) instanceof SQLShowTablesStatement) ) {				
//				if ( !result.getViolations().isEmpty()) {				
//					isPassed = false;
//					ALARM.warn("Firewall to intercept the '" + user + "' unsafe SQL , errMsg:"
//							+ result.getViolations().get(0).getMessage() +
//							" \r\n " + sql);
//		        }				
//			}
			
			if ( !result.getViolations().isEmpty()) {				
				isPassed = false;
				ALARM.warn("Firewall to intercept the '" + user + "' unsafe SQL , errMsg:"
						+ result.getViolations().get(0).getMessage() +
						" \r\n " + sql);
	        }	
			
			
		}
		return isPassed;
	}

	// 审计SQL权限
	@Override
	public boolean checkDmlPrivilege(String user, String schema, String sql) {

		if ( schema == null ) {
			return true;
		}
		
		boolean isPassed = false;

		ShardingCatConfig conf = ShardingCatServer.getInstance().getConfig();
		UserConfig userConfig = conf.getUsers().get(user);
		if (userConfig != null) {
			
			UserPrivilegesConfig userPrivilege = userConfig.getPrivilegesConfig();
			if ( userPrivilege != null && userPrivilege.isCheck() ) {				
			
				UserPrivilegesConfig.SchemaPrivilege schemaPrivilege = userPrivilege.getSchemaPrivilege( schema );
				if ( schemaPrivilege != null ) {
		
					String tableName = null;
					int index = -1;
					
					//TODO 此处待优化，寻找更优SQL 解析器
					
					//修复bug
					// https://github.com/alibaba/druid/issues/1309
					//com.alibaba.druid.sql.parser.ParserException: syntax error, error in :'begin',expect END, actual EOF begin
					if ( sql != null && sql.length() == 5 && sql.equalsIgnoreCase("begin") ) {
						return true;
					}
					
					SQLStatementParser parser = new ShardingCatStatementParser(sql);
					SQLStatement stmt = parser.parseStatement();
					
					if (stmt instanceof MySqlReplaceStatement || stmt instanceof SQLInsertStatement ) {		
						index = 0;
					} else if (stmt instanceof SQLUpdateStatement ) {
						index = 1;
					} else if (stmt instanceof SQLSelectStatement ) {
						index = 2;
					} else if (stmt instanceof SQLDeleteStatement ) {
						index = 3;
					}
					
					if ( index > -1) {
						
						SchemaStatVisitor schemaStatVisitor = new ShardingCatSchemaStatVisitor();
						stmt.accept(schemaStatVisitor);
						String key = schemaStatVisitor.getCurrentTable();
						if ( key != null ) {
							
							if (key.contains("`")) {
								key = key.replaceAll("`", "");
							}
							
							int dotIndex = key.indexOf(".");
							if (dotIndex > 0) {
								tableName = key.substring(dotIndex + 1);
							} else {
								tableName = key;
							}							
							
							//获取table 权限, 此处不需要检测空值, 无设置则自动继承父级权限
							UserPrivilegesConfig.TablePrivilege tablePrivilege = schemaPrivilege.getTablePrivilege( tableName );
							if ( tablePrivilege.getDml()[index] > 0 ) {
								isPassed = true;
							}
							
						} else {
							//skip
							isPassed = true;
						}
						
						
					} else {						
						//skip
						isPassed = true;
					}
					
				} else {					
					//skip
					isPassed = true;
				}
				
			} else {
				//skip
				isPassed = true;
			}

		} else {
			//skip
			isPassed = true;
		}
		
		if( !isPassed ) {
			 ALARM.error(new StringBuilder().append(Alarms.DML_ATTACK ).append("[sql=").append( sql )
                     .append(",user=").append(user).append(']').toString());
		}
		
		return isPassed;
	}	
	
}