package io.shardingcat.migrate;

import java.io.Serializable;

/**
 * Created by magicdoom on 2016/9/28.
 */
public class TaskNode implements Serializable {
    private String sql;
    private int status ;    //0=init    1=start    2=prepare switch    3=commit sucess   4=error     5=clean  sucess     6=error process end
    private String schema;
    private String table;
    private String add;

    public String getSql() {
        return sql;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getAdd() {
        return add;
    }

    public void setAdd(String add) {
        this.add = add;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
