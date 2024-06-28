
package io.shardingcat.sqlengine.mpp;

import io.shardingcat.net.mysql.RowDataPacket;
import io.shardingcat.sqlengine.mpp.tmp.RowDataSorter;


public class RangRowDataPacketSorter extends RowDataSorter {
    public RangRowDataPacketSorter(OrderCol[] orderCols) {
        super(orderCols);
    }

    public boolean ascDesc(int byColumnIndex) {
        if (this.orderCols[byColumnIndex].orderType == OrderCol.COL_ORDER_TYPE_ASC) {// 升序
            return true;
        }
        return false;
    }

    public int compareRowData(RowDataPacket l, RowDataPacket r, int byColumnIndex) {
        byte[] left = l.fieldValues.get(this.orderCols[byColumnIndex].colMeta.colIndex);
        byte[] right = r.fieldValues.get(this.orderCols[byColumnIndex].colMeta.colIndex);

        return compareObject(left, right, this.orderCols[byColumnIndex]);
    }
}