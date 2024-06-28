
package io.shardingcat.config.model;

import java.util.Set;

/**
 * @author shardingcat
 */
public class UserConfig {

    private String name;
    private String password;						//明文
    private String encryptPassword; 				//密文
    private int benchmark = 0;						// 负载限制, 默认0表示不限制
    private UserPrivilegesConfig privilegesConfig;	//SQL表级的增删改查权限控制
    
    /**
     * 是否无密码登陆的默认账户
     */
    private boolean defaultAccount = false;
    private boolean readOnly = false;
    
    public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	private Set<String> schemas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	public int getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(int benchmark) {
		this.benchmark = benchmark;
	}

	public Set<String> getSchemas() {
        return schemas;
    }

	public String getEncryptPassword() {
		return this.encryptPassword;
	}

	public void setEncryptPassword(String encryptPassword) {
		this.encryptPassword = encryptPassword;
	}

	public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }
	
	public UserPrivilegesConfig getPrivilegesConfig() {
		return privilegesConfig;
	}
	
	public void setPrivilegesConfig(UserPrivilegesConfig privilegesConfig) {
		this.privilegesConfig = privilegesConfig;
	}

	
	public boolean isDefaultAccount() {
		return defaultAccount;
	}

	public void setDefaultAccount(boolean defaultAccount) {
		this.defaultAccount = defaultAccount;
	}

	@Override
	public String toString() {
		return "UserConfig [name=" + name + ", password=" + password + ", encryptPassword=" + encryptPassword
				+ ", benchmark=" + benchmark + ", privilegesConfig=" + privilegesConfig + ", defaultAccount="
				+ defaultAccount + ", readOnly=" + readOnly + ", schemas=" + schemas + "]";
	}

	 
	
	

}