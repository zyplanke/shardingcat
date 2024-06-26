package io.shardingcat.backend.jdbc.mongodb;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;

public class MongoData {
	
   private DBCursor cursor;
   private long count;
   private String table;
   private DBObject groupby;
   
   private HashMap<String,Integer> map = new HashMap<String,Integer>(); 
   private boolean type=false;
   
   public MongoData(){
	 this.count=0;
	 this.cursor=null;
   }
   
   public long getCount() {
	  return this.count;
   } 
   
   
   public void setCount(long count)  {
	  this.count=count;		
   } 
   
   public String getTable() {
	  return this.table;
   }   
   
   public void setTable(String table)  {
	  this.table=table;		
   } 
   
   public DBObject getGrouyBy() {
	  return this.groupby;
   }   
   
   public BasicDBList getGrouyBys() {
	   if (this.groupby instanceof BasicDBList) {
		  return (BasicDBList)this.groupby;  
	   }	     
	   else {
	     return null;
	   }
   }    
   public void setGrouyBy(DBObject gb)  {
	  this.groupby=gb;	
	  this.type=true;
	  if (gb instanceof BasicDBList) {
		Object gb2=((BasicDBList)gb).get(0);
		if (gb2 instanceof DBObject) { 
        for (String field :((DBObject)gb2).keySet()) {            
          Object val = ((DBObject)gb2).get(field);	
          setField(field,getObjectToType(val));
        }
	   }
	  }
   } 
   
   public static int getObjectToType(Object ob){
		if (ob instanceof Integer) {
			return Types.INTEGER;
		}
		else if (ob instanceof Boolean) {
			return Types.BOOLEAN;
		}
		else if (ob instanceof Byte) {
			return Types.BIT;
		}	
		else if (ob instanceof Short) {
			return Types.INTEGER;
		}	
		else if (ob instanceof Float) {
			return Types.FLOAT;
		}			
		else if (ob instanceof Long) {
			return Types.BIGINT;
		}
		else if (ob instanceof Double) {
			return Types.DOUBLE;
		}			
		else if (ob instanceof Date) {
			return Types.DATE;
		}	
		else if (ob instanceof Time) {
			return Types.TIME;
		}	
		else if (ob instanceof Timestamp) {
			return Types.TIMESTAMP;
		}
		else if (ob instanceof String) {
			return Types.VARCHAR;
		}			
		else  {
			return Types.VARCHAR;
		}	   
   }
      
   public void setField(String field,int ftype)  {
	   map.put(field, ftype);
   } 
   
   public HashMap<String,Integer> getFields()  {
	   return this.map;
   } 
   
   public boolean getType() {
	  return this.type;
   }  
   
   public DBCursor getCursor() {
	  return this.cursor;
   }  
  
   public DBCursor setCursor(DBCursor cursor)  {
	   return this.cursor=cursor;		
   }    
   
}
