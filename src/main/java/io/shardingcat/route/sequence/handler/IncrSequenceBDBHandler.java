
package io.shardingcat.route.sequence.handler;

import java.util.Map;

/**
 * BDB 数据库实现递增序列号
 * 
 * @author <a href="http://www.micmiu.com">Michael</a>
 * @time Create on 2013-12-29 下午11:05:44
 * @version 1.0
 */
public class IncrSequenceBDBHandler extends IncrSequenceHandler {

	private static class IncrSequenceBDBHandlerHolder {
		private static final IncrSequenceBDBHandler instance = new IncrSequenceBDBHandler();
	}

	public static IncrSequenceBDBHandler getInstance() {
		return IncrSequenceBDBHandlerHolder.instance;
	}

	private IncrSequenceBDBHandler() {
	}

	@Override
	public Map<String, String> getParaValMap(String prefixName) {
		
		return null;
	}

	@Override
	public Boolean fetchNextPeriod(String prefixName) {
		
		return null;
	}

	@Override
	public Boolean updateCURIDVal(String prefixName, Long val) {
		
		return null;
	}

}