
package io.shardingcat.config;

/**
 * @author shardingcat
 */
public abstract class Versions {

    /**协议版本**/
    public static final byte PROTOCOL_VERSION = 10;

    /**服务器版本**/
    public static byte[] SERVER_VERSION = "shardingcat-v1.0_20240628-2024-06-28T19:52:05+0800".getBytes();

    public static void setServerVersion(String version) {
        byte[] mysqlVersionPart = version.getBytes();
        int startIndex;
        for (startIndex = 0; startIndex < SERVER_VERSION.length; startIndex++) {
            if (SERVER_VERSION[startIndex] == '-')
                break;
        }

        // 重新拼接Shardingcat version字节数组
        byte[] newShardingCatVersion = new byte[mysqlVersionPart.length + SERVER_VERSION.length - startIndex];
        System.arraycopy(mysqlVersionPart, 0, newShardingCatVersion, 0, mysqlVersionPart.length);
        System.arraycopy(SERVER_VERSION, startIndex, newShardingCatVersion, mysqlVersionPart.length,
                SERVER_VERSION.length - startIndex);
        SERVER_VERSION = newShardingCatVersion;
    }
}
