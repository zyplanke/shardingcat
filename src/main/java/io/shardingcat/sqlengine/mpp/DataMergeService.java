package io.shardingcat.sqlengine.mpp;



import io.shardingcat.ShardingCatServer;
import io.shardingcat.backend.mysql.BufferUtil;
import io.shardingcat.backend.mysql.nio.handler.MultiNodeQueryHandler;
import io.shardingcat.net.mysql.EOFPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.route.RouteResultset;
import io.shardingcat.route.RouteResultsetNode;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.sqlengine.mpp.tmp.RowDataSorter;
import io.shardingcat.util.StringUtil;

import org.apache.log4j.Logger;



import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data merge service handle data Min,Max,AVG group 、order by 、limit
 * 
 * @author wuzhih /modify by coder_czp/2015/11/2
 * 
 * Fixbug: shardingcat sql timeout and hang problem.
 * @author Uncle-pan
 * @since 2016-03-23
 * 
 */
public class DataMergeService extends AbstractDataNodeMerge {

	private RowDataSorter sorter;
	private RowDataPacketGrouper grouper;
	private Map<String, LinkedList<RowDataPacket>> result = new HashMap<String, LinkedList<RowDataPacket>>();
	private static Logger LOGGER = Logger.getLogger(DataMergeService.class);
	private ConcurrentHashMap<String, Boolean> canDiscard = new ConcurrentHashMap<String, Boolean>();
	public DataMergeService(MultiNodeQueryHandler handler, RouteResultset rrs) {
		super(handler,rrs);

		for (RouteResultsetNode node : rrs.getNodes()) {
			result.put(node.getName(), new LinkedList<RowDataPacket>());
		}
	}


	/**
	 * @param columToIndx
	 * @param fieldCount
     */
	public void onRowMetaData(Map<String, ColMeta> columToIndx, int fieldCount) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("field metadata keys:" + columToIndx.keySet());
			LOGGER.debug("field metadata values:" + columToIndx.values());
		}


		int[] groupColumnIndexs = null;
		this.fieldCount = fieldCount;

		if (rrs.getGroupByCols() != null) {
		
			groupColumnIndexs = toColumnIndex(rrs.getGroupByCols(), columToIndx);
		}

		if (rrs.getHavingCols() != null) {
			ColMeta colMeta = columToIndx.get(rrs.getHavingCols().getLeft()
					.toUpperCase());
			if (colMeta != null) {
				rrs.getHavingCols().setColMeta(colMeta);
			}
		}

		if (rrs.isHasAggrColumn()) {
			List<MergeCol> mergCols = new LinkedList<MergeCol>();
			Map<String, Integer> mergeColsMap = rrs.getMergeCols();



			if (mergeColsMap != null) {
				for (Map.Entry<String, Integer> mergEntry : mergeColsMap
						.entrySet()) {
					String colName = mergEntry.getKey().toUpperCase();
					int type = mergEntry.getValue();
					if (MergeCol.MERGE_AVG == type) {
					
						ColMeta sumColMeta = columToIndx.get(colName + "SUM");
						ColMeta countColMeta = columToIndx.get(colName
								+ "COUNT");
						if (sumColMeta != null && countColMeta != null) {
							ColMeta colMeta = new ColMeta(sumColMeta.colIndex,
									countColMeta.colIndex,
									sumColMeta.getColType());
							colMeta.decimals = sumColMeta.decimals; // 保存精度
							mergCols.add(new MergeCol(colMeta, mergEntry
									.getValue()));
						}
					} else {
						
						ColMeta colMeta = columToIndx.get(colName);
						mergCols.add(new MergeCol(colMeta, mergEntry.getValue()));
					}
				}
			}
			// add no alias merg column
			for (Map.Entry<String, ColMeta> fieldEntry : columToIndx.entrySet()) {
				String colName = fieldEntry.getKey();
				int result = MergeCol.tryParseAggCol(colName);
				if (result != MergeCol.MERGE_UNSUPPORT
						&& result != MergeCol.MERGE_NOMERGE) {
					mergCols.add(new MergeCol(fieldEntry.getValue(), result));
				}
			}

		
			grouper = new RowDataPacketGrouper(groupColumnIndexs,
					mergCols.toArray(new MergeCol[mergCols.size()]),
					rrs.getHavingCols());
		}

		if (rrs.getOrderByCols() != null) {
			LinkedHashMap<String, Integer> orders = rrs.getOrderByCols();
			OrderCol[] orderCols = new OrderCol[orders.size()];
			int i = 0;
			for (Map.Entry<String, Integer> entry : orders.entrySet()) {
				String key = StringUtil.removeBackquote(entry.getKey()
						.toUpperCase());
				ColMeta colMeta = columToIndx.get(key);
				if (colMeta == null) {
					throw new IllegalArgumentException(
							"all columns in order by clause should be in the selected column list!"
									+ entry.getKey());
				}
				orderCols[i++] = new OrderCol(colMeta, entry.getValue());
			}

			RowDataSorter tmp = new RowDataSorter(orderCols);
			tmp.setLimit(rrs.getLimitStart(), rrs.getLimitSize());
			sorter = tmp;
		}

		if (ShardingCatServer.getInstance().
				getConfig().getSystem().
				getUseStreamOutput() == 1
				&& grouper == null
				&& sorter == null) {
			setStreamOutputResult(true);
		}else {
			setStreamOutputResult(false);
		}
	}


	/**
	 * release resources
	 */
	public void clear() {
		result.clear();
		grouper = null;
		sorter = null;
	}

	@Override
	public void run() {
		// sort-or-group: no need for us to using multi-threads, because
		//both sorter and group are synchronized!!
		// @author Uncle-pan
		// @since 2016-03-23
		if(!running.compareAndSet(false, true)){
			return;
		}
		// eof handler has been placed to "if (pack == END_FLAG_PACK){}" in for-statement
		// @author Uncle-pan
		// @since 2016-03-23
		boolean nulpack = false;
		try{
			// loop-on-packs
			for (; ; ) {
				final PackWraper pack = packs.poll();
				// async: handling row pack queue, this business thread should exit when no pack
				// @author Uncle-pan
				// @since 2016-03-23
				if(pack == null){
					nulpack = true;
					break;
				}
				// eof: handling eof pack and exit
				if (pack == END_FLAG_PACK) {



					final int warningCount = 0;
					final EOFPacket eofp   = new EOFPacket();
					final ByteBuffer eof   = ByteBuffer.allocate(9);
					BufferUtil.writeUB3(eof, eofp.calcPacketSize());
					eof.put(eofp.packetId);
					eof.put(eofp.fieldCount);
					BufferUtil.writeUB2(eof, warningCount);
					BufferUtil.writeUB2(eof, eofp.status);
					final ServerConnection source = multiQueryHandler.getSession().getSource();
					final byte[] array = eof.array();
					multiQueryHandler.outputMergeResult(source, array, getResults(array));
					break;
				}


				// merge: sort-or-group, or simple add
				final RowDataPacket row = new RowDataPacket(fieldCount);
				row.read(pack.rowData);

				if (grouper != null) {
					grouper.addRow(row);
				} else if (sorter != null) {
					if (!sorter.addRow(row)) {
						canDiscard.put(pack.dataNode,true);
					}
				} else {
					result.get(pack.dataNode).add(row);
				}
			}// rof
		}catch(final Exception e){
			multiQueryHandler.handleDataProcessException(e);
		}finally{
			running.set(false);
		}
		// try to check packs, it's possible that adding a pack after polling a null pack
		//and before this time pointer!!
		// @author Uncle-pan
		// @since 2016-03-23
		if(nulpack && !packs.isEmpty()){
			this.run();
		}
	}
	


	/**
	 * return merged data
	 * @return (最多i*(offset+size)行数据)
	 */
	public List<RowDataPacket> getResults(byte[] eof) {
	
		List<RowDataPacket> tmpResult = null;

		if (this.grouper != null) {
			tmpResult = grouper.getResult();
			grouper = null;
		}

		
		if (sorter != null) {
			
			if (tmpResult != null) {
				Iterator<RowDataPacket> itor = tmpResult.iterator();
				while (itor.hasNext()) {
					sorter.addRow(itor.next());
					itor.remove();
				}
			}
			tmpResult = sorter.getSortedResult();
			sorter = null;
		}


		
		//no grouper and sorter
		if(tmpResult == null){
			tmpResult = new LinkedList<RowDataPacket>();
			for (RouteResultsetNode node : rrs.getNodes()) {
				tmpResult.addAll(result.get(node.getName()));
			}
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("prepare mpp merge result for " + rrs.getStatement());
		}
		return tmpResult;
	}
}

