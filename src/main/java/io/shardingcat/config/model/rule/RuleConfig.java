
package io.shardingcat.config.model.rule;

import io.shardingcat.route.function.AbstractPartitionAlgorithm;

import java.io.Serializable;

/**
 * 分片规则，column是用于分片的数据库物理字段
 * @author shardingcat
 */
public class RuleConfig implements Serializable {
	private final String column;
	private final String functionName;
	private AbstractPartitionAlgorithm ruleAlgorithm;

	public RuleConfig(String column, String functionName) {
		if (functionName == null) {
			throw new IllegalArgumentException("functionName is null");
		}
		this.functionName = functionName;
		if (column == null || column.length() <= 0) {
			throw new IllegalArgumentException("no rule column is found");
		}
		this.column = column;
	}

	

	public AbstractPartitionAlgorithm getRuleAlgorithm() {
		return ruleAlgorithm;
	}



	public void setRuleAlgorithm(AbstractPartitionAlgorithm ruleAlgorithm) {
		this.ruleAlgorithm = ruleAlgorithm;
	}



	/**
	 * @return unmodifiable, upper-case
	 */
	public String getColumn() {
		return column;
	}

	public String getFunctionName() {
		return functionName;
	}


	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((functionName == null) ? 0 : functionName.hashCode());
		result = prime * result + ((ruleAlgorithm == null) ? 0 : ruleAlgorithm.hashCode());
		return result;
	}


	//huangyiming add 判断分片规则是否相同,暂时根据这个去判断
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RuleConfig other = (RuleConfig) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (functionName == null) {
			if (other.functionName != null)
				return false;
		} else if (!functionName.equals(other.functionName))
			return false;
		if (ruleAlgorithm == null) {
			if (other.ruleAlgorithm != null)
				return false;
		} else if (!ruleAlgorithm.equals(other.ruleAlgorithm))
			return false;
		return true;
	}

	

}
