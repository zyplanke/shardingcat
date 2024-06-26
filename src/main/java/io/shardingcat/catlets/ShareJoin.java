package io.shardingcat.catlets;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import io.shardingcat.backend.mysql.nio.handler.MiddlerQueryResultHandler;
import io.shardingcat.backend.mysql.nio.handler.MiddlerResultHandler;
import io.shardingcat.cache.LayerCachePool;
import io.shardingcat.config.ErrorCode;
import io.shardingcat.config.Fields;
import io.shardingcat.config.model.SchemaConfig;
import io.shardingcat.config.model.SystemConfig;
import io.shardingcat.net.mysql.FieldPacket;
import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.route.RouteResultset;
import io.shardingcat.route.RouteResultsetNode;
import io.shardingcat.route.factory.RouteStrategyFactory;
import io.shardingcat.server.NonBlockingSession;
import io.shardingcat.server.ServerConnection;
import io.shardingcat.server.parser.ServerParse;
import io.shardingcat.sqlengine.AllJobFinishedListener;
import io.shardingcat.sqlengine.EngineCtx;
import io.shardingcat.sqlengine.SQLJobHandler;
import io.shardingcat.util.ByteUtil;
import io.shardingcat.util.ResultSetUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**  
 * 功能详细描述:分片join
 * @author sohudo[http://blog.csdn.net/wind520]
 * @create 2015年01月22日 下午6:50:23 
 * @version 0.0.1
 */

public class ShareJoin implements Catlet {
	private EngineCtx ctx;
	private RouteResultset rrs ;
	private JoinParser joinParser;
	
	private Map<String, byte[]> rows = new ConcurrentHashMap<String, byte[]>();
	private Map<String,String> ids = new ConcurrentHashMap<String,String>();
	//private ConcurrentLinkedQueue<String> ids = new ConcurrentLinkedQueue<String>();
	
	private List<byte[]> fields; //主表的字段
	private ArrayList<byte[]> allfields;//所有的字段
	private boolean isMfield=false;
	private int mjob=0;
	private int maxjob=0;
	private int joinindex=0;//关联join表字段的位置
	private int sendField=0;
	private boolean childRoute=false;
	private boolean jointTableIsData=false;
	// join 字段的类型，一般情况都是int, long; 增加该字段为了支持非int,long类型的(一般为varchar)joinkey的sharejoin
 	// 参见：io.shardingcat.server.packet.FieldPacket 属性： public int type;
 	// 参见：http://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnDefinition
 	private int joinKeyType = Fields.FIELD_TYPE_LONG; // 默认 join 字段为int型
 	
	//重新路由使用
	private SystemConfig sysConfig; 
	private SchemaConfig schema;
	private int sqltype; 
	private String charset; 
	private ServerConnection sc;	
	private LayerCachePool cachePool;
	public void setRoute(RouteResultset rrs){
		this.rrs =rrs;
	}	
	
	public void route(SystemConfig sysConfig, SchemaConfig schema,int sqlType, String realSQL, String charset, ServerConnection sc,	LayerCachePool cachePool) {
		int rs = ServerParse.parse(realSQL);
		this.sqltype = rs & 0xff;
		this.sysConfig=sysConfig; 
		this.schema=schema;
		this.charset=charset; 
		this.sc=sc;	
		this.cachePool=cachePool;		
		try {
		 //  RouteStrategy routes=RouteStrategyFactory.getRouteStrategy();	
		  // rrs =RouteStrategyFactory.getRouteStrategy().route(sysConfig, schema, sqlType2, realSQL,charset, sc, cachePool);		   
			MySqlStatementParser parser = new MySqlStatementParser(realSQL);			
			SQLStatement statement = parser.parseStatement();
			if(statement instanceof SQLSelectStatement) {
			   SQLSelectStatement st=(SQLSelectStatement)statement;
			   SQLSelectQuery sqlSelectQuery =st.getSelect().getQuery();
				if(sqlSelectQuery instanceof MySqlSelectQueryBlock) {
					MySqlSelectQueryBlock mysqlSelectQuery = (MySqlSelectQueryBlock)st.getSelect().getQuery();
					joinParser=new JoinParser(mysqlSelectQuery,realSQL);
					joinParser.parser();
				}	
			}
		   /*	
		   if (routes instanceof DruidMysqlRouteStrategy) {
			   SQLSelectStatement st=((DruidMysqlRouteStrategy) routes).getSQLStatement();
			   SQLSelectQuery sqlSelectQuery =st.getSelect().getQuery();
				if(sqlSelectQuery instanceof MySqlSelectQueryBlock) {
					MySqlSelectQueryBlock mysqlSelectQuery = (MySqlSelectQueryBlock)st.getSelect().getQuery();
					joinParser=new JoinParser(mysqlSelectQuery,realSQL);
					joinParser.parser();
				}
		   }
		   */
		} catch (Exception e) {
		
		}
	}
	private void getRoute(String sql){
		try {
		  if (joinParser!=null){
			rrs =RouteStrategyFactory.getRouteStrategy().route(sysConfig, schema, sqltype,sql,charset, sc, cachePool);
		  }
		} catch (Exception e) {
			
		}
	}
	private String[] getDataNodes(){		
		String[] dataNodes =new String[rrs.getNodes().length] ;
		for (int i=0;i<rrs.getNodes().length;i++){
			dataNodes[i]=rrs.getNodes()[i].getName();
		}
		return dataNodes;
	}
	private String getDataNode(String[] dataNodes){
		String dataNode="";
		for (int i=0;i<dataNodes.length;i++){
			dataNode+=dataNodes[i]+",";
		}
		return dataNode;
	}
	public void processSQL(String sql, EngineCtx ctx) {
		String ssql=joinParser.getSql();
		getRoute(ssql);
		RouteResultsetNode[] nodes = rrs.getNodes();
		if (nodes == null || nodes.length == 0 || nodes[0].getName() == null
				|| nodes[0].getName().equals("")) {
			ctx.getSession().getSource().writeErrMessage(ErrorCode.ER_NO_DB_ERROR,
					"No dataNode found ,please check tables defined in schema:"
							+ ctx.getSession().getSource().getSchema());
			return;
		} 
		this.ctx=ctx;
		String[] dataNodes =getDataNodes();
		maxjob=dataNodes.length;
	 

    	//huangyiming
		ShareDBJoinHandler joinHandler = new ShareDBJoinHandler(this,joinParser.getJoinLkey(),sc.getSession2());		
		ctx.executeNativeSQLSequnceJob(dataNodes, ssql, joinHandler);
    	EngineCtx.LOGGER.info("Catlet exec:"+getDataNode(getDataNodes())+" sql:" +ssql);

		ctx.setAllJobFinishedListener(new AllJobFinishedListener() {
			@Override
			public void onAllJobFinished(EngineCtx ctx) {				
				 if (!jointTableIsData) {
					 ctx.writeHeader(fields);
				 }
				 
				 MiddlerResultHandler middlerResultHandler = sc.getSession2().getMiddlerResultHandler();

					if(  middlerResultHandler !=null ){
						//sc.getSession2().setCanClose(false);
						middlerResultHandler.secondEexcute(); 
					} else{
						ctx.writeEof();
					}
				EngineCtx.LOGGER.info("发送数据OK"); 
			}
		});
	}
	
    public void putDBRow(String id,String nid, byte[] rowData,int findex){
    	rows.put(id, rowData);	
    	ids.put(id, nid);
    	joinindex=findex;
		//ids.offer(nid);
		int batchSize = 999;
		// 满1000条，发送一个查询请求
		if (ids.size() > batchSize) {
			createQryJob(batchSize);
		}            	
    }
    
    public void putDBFields(List<byte[]> mFields){
    	 if (!isMfield){
    		 fields=mFields; 
    	 }    	
    }    

   public void endJobInput(String dataNode, boolean failed){
	   mjob++;
	   if (mjob>=maxjob){
		 createQryJob(Integer.MAX_VALUE);
	     ctx.endJobInput();
	   }
	  // EngineCtx.LOGGER.info("完成"+mjob+":" + dataNode+" failed:"+failed);
   }
   
	//private void createQryJob(String dataNode,int batchSize) {	
	private void createQryJob(int batchSize) {	
		int count = 0;
		Map<String, byte[]> batchRows = new ConcurrentHashMap<String, byte[]>();
		String theId = null;
		StringBuilder sb = new StringBuilder().append('(');
		String svalue="";
		for(Map.Entry<String,String> e: ids.entrySet() ){
			theId=e.getKey();
			byte[] rowbyte = rows.remove(theId);
			if(rowbyte!=null){
				batchRows.put(theId, rowbyte);
			}			
			if (!svalue.equals(e.getValue())){
				if(joinKeyType == Fields.FIELD_TYPE_VAR_STRING 
						|| joinKeyType == Fields.FIELD_TYPE_STRING){ // joinkey 为varchar
						sb.append("'").append(e.getValue()).append("'").append(','); // ('digdeep','yuanfang') 
				}else{ // 默认joinkey为int/long
					sb.append(e.getValue()).append(','); // (1,2,3) 
				}
			}
			svalue=e.getValue();
			if (count++ > batchSize) {
				break;
			}			
		}
		/*
		while ((theId = ids.poll()) != null) {
			batchRows.put(theId, rows.remove(theId));
			sb.append(theId).append(',');
			if (count++ > batchSize) {
				break;
			}
		}
		*/
		if (count == 0) {
			return;
		}
		jointTableIsData=true;
		sb.deleteCharAt(sb.length() - 1).append(')');
		String sql = String.format(joinParser.getChildSQL(), sb);
		//if (!childRoute){
		  getRoute(sql);
		 //childRoute=true;
		//}
		ctx.executeNativeSQLParallJob(getDataNodes(),sql, new ShareRowOutPutDataHandler(this,fields,joinindex,joinParser.getJoinRkey(), batchRows,ctx.getSession()));
		EngineCtx.LOGGER.info("SQLParallJob:"+getDataNode(getDataNodes())+" sql:" + sql);		
	}  
	public void writeHeader(String dataNode,List<byte[]> afields, List<byte[]> bfields) {
		sendField++;
		if (sendField==1){		  	
			//huangyiming add 只是中间过程数据不能发送给客户端
			MiddlerResultHandler middlerResultHandler = sc.getSession2().getMiddlerResultHandler();
 			if(middlerResultHandler ==null ){
				 ctx.writeHeader(afields, bfields);
 			}  
 		  setAllFields(afields, bfields);
		 // EngineCtx.LOGGER.info("发送字段2:" + dataNode);
		}
		
	}
	private void setAllFields(List<byte[]> afields, List<byte[]> bfields){		
		allfields=new ArrayList<byte[]>();
		for (byte[] field : afields) {
			allfields.add(field);
		}
		//EngineCtx.LOGGER.info("所有字段2:" +allfields.size());
		for (int i=1;i<bfields.size();i++){
			allfields.add(bfields.get(i));
		}
		
	}
	public List<byte[]> getAllFields(){		
		return allfields;
	}
	public void writeRow(RowDataPacket rowDataPkg){
		ctx.writeRow(rowDataPkg);
	}
	
	public int getFieldIndex(List<byte[]> fields,String fkey){
		int i=0;
		for (byte[] field :fields) {	
			  FieldPacket fieldPacket = new FieldPacket();
			  fieldPacket.read(field);	
			  if (ByteUtil.getString(fieldPacket.orgName).equals(fkey)){
				  joinKeyType = fieldPacket.type;
				  return i;				  
			  }
			  i++;
			}
		return i;		
	}	
}

class ShareDBJoinHandler implements SQLJobHandler {
	private List<byte[]> fields;
	private final ShareJoin ctx;
	private String joinkey;
	private NonBlockingSession session;
	public ShareDBJoinHandler(ShareJoin ctx,String joinField,NonBlockingSession session) {
		super();
		this.ctx = ctx;
		this.joinkey=joinField;
		this.session = session;
		//EngineCtx.LOGGER.info("二次查询:"  +" sql:" + querySQL+"/"+joinkey);
	}

	//private Map<String, byte[]> rows = new ConcurrentHashMap<String, byte[]>();
	//private ConcurrentLinkedQueue<String> ids = new ConcurrentLinkedQueue<String>();

	@Override
	public void onHeader(String dataNode, byte[] header, List<byte[]> fields) {
		this.fields = fields;
		ctx.putDBFields(fields);
	}
	

	/*
	public static String getFieldNames(List<byte[]> fields){
		String str="";
		for (byte[] field :fields) {	
		  FieldPacket fieldPacket = new FieldPacket();
		  fieldPacket.read(field);	
		  str+=ByteUtil.getString(fieldPacket.name)+",";
		}
		return str;
	}
	
	public static String getFieldName(byte[] field){
		FieldPacket fieldPacket = new FieldPacket();
		fieldPacket.read(field);	
		return ByteUtil.getString(fieldPacket.name);
	}
	*/
	@Override
	public boolean onRowData(String dataNode, byte[] rowData) {
		int fid=this.ctx.getFieldIndex(fields,joinkey);
		String id = ResultSetUtil.getColumnValAsString(rowData, fields, 0);//主键，默认id
		String nid = ResultSetUtil.getColumnValAsString(rowData, fields, fid);
		// 放入结果集
		//rows.put(id, rowData);
		ctx.putDBRow(id,nid, rowData,fid);
		return false;
	}

	@Override
	public void finished(String dataNode, boolean failed, String errorMsg) {
		if(failed){
			session.getSource().writeErrMessage(ErrorCode.ER_UNKNOWN_ERROR, errorMsg);
		}else{
			ctx.endJobInput(dataNode,failed);
		}
	}

}

class ShareRowOutPutDataHandler implements SQLJobHandler {
	private final List<byte[]> afields;
	private List<byte[]> bfields;
	private final ShareJoin ctx;
	private final Map<String, byte[]> arows;
	private int joinL;//A表(左边)关联字段的位置
	private int joinR;//B表(右边)关联字段的位置
	private String joinRkey;//B表(右边)关联字段
	public NonBlockingSession session;

	public ShareRowOutPutDataHandler(ShareJoin ctx,List<byte[]> afields,int joini,String joinField,Map<String, byte[]> arows,NonBlockingSession session) {
		super();
		this.afields = afields;
		this.ctx = ctx;
		this.arows = arows;		
		this.joinL =joini;
		this.joinRkey= joinField;
		this.session = session;
		//EngineCtx.LOGGER.info("二次查询:" +arows.size()+ " afields："+FenDBJoinHandler.getFieldNames(afields));
    }

	@Override
	public void onHeader(String dataNode, byte[] header, List<byte[]> bfields) {
		  this.bfields=bfields;		
		  joinR=this.ctx.getFieldIndex(bfields,joinRkey);
		  MiddlerResultHandler middlerResultHandler = session.getMiddlerResultHandler();

			if(  middlerResultHandler ==null ){
				  ctx.writeHeader(dataNode,afields, bfields);

			} 
 	}

	//不是主键，获取join左边的的记录
	private byte[] getRow(Map<String, byte[]> batchRowsCopy,String value,int index){
		for(Map.Entry<String,byte[]> e: batchRowsCopy.entrySet() ){
			String key=e.getKey();
			RowDataPacket rowDataPkg = ResultSetUtil.parseRowData(e.getValue(), afields);
			byte[] columnValue = rowDataPkg.fieldValues.get(index);
			if(columnValue == null )
				continue;
			
			String id = ByteUtil.getString(columnValue);
			if (id.equals(value)){
				return batchRowsCopy.remove(key);
			}
		}
		return null;
	}

	@Override
	public boolean onRowData(String dataNode, byte[] rowData) {
		RowDataPacket rowDataPkgold = ResultSetUtil.parseRowData(rowData, bfields);
		//拷贝一份batchRows
		Map<String, byte[]> batchRowsCopy = new ConcurrentHashMap<String, byte[]>();
		batchRowsCopy.putAll(arows);
		// 获取Id字段，
		String id = ByteUtil.getString(rowDataPkgold.fieldValues.get(joinR));
		// 查找ID对应的A表的记录
		byte[] arow = getRow(batchRowsCopy,id,joinL);//arows.remove(id);
//		byte[] arow = getRow(id,joinL);//arows.remove(id);
		while (arow!=null) {
			RowDataPacket rowDataPkg = ResultSetUtil.parseRowData(arow,afields );//ctx.getAllFields());
			for (int i=1;i<rowDataPkgold.fieldCount;i++){
				// 设置b.name 字段
				byte[] bname = rowDataPkgold.fieldValues.get(i);
				rowDataPkg.add(bname);
				rowDataPkg.addFieldCount(1);
			}
			//RowData(rowDataPkg);
			// huangyiming add
			MiddlerResultHandler middlerResultHandler = session.getMiddlerResultHandler();
			if(null == middlerResultHandler ){
				ctx.writeRow(rowDataPkg);
			}else{
				
				 if(middlerResultHandler instanceof MiddlerQueryResultHandler){
					// if(middlerResultHandler.getDataType().equalsIgnoreCase("string")){
						 byte[] columnData = rowDataPkg.fieldValues.get(0);
						 if(columnData !=null && columnData.length >0){
 							 String rowValue =    new String(columnData);
							 middlerResultHandler.add(rowValue);	
						 }
				   //}
				 }
				
			} 
			
			arow = getRow(batchRowsCopy,id,joinL);
//		   arow = getRow(id,joinL);
		}
		return false;
	}
	

	@Override
	public void finished(String dataNode, boolean failed, String errorMsg) {
		if(failed){
			session.getSource().writeErrMessage(ErrorCode.ER_UNKNOWN_ERROR, errorMsg);
		}
	}
}