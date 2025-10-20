
package io.shardingcat.config;
import io.shardingcat.cache.DefaultLayedCachePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author shardingcat
 */
public class Versions {
    private static final Logger LOGGER = LoggerFactory.getLogger(Versions.class);

    /**协议版本**/
    public static final byte PROTOCOL_VERSION = 10;

    /**本ShardingCat服务器版本**/
    public static byte[] SERVER_VERSION = "NOT_SET_VERSION".getBytes();

    static {
        // 从VERSION.txt文件中读出内容，以初始化本类静态变量值
        Properties prop = new Properties();
        try (InputStream versionTxtFile = Files.newInputStream(Paths.get("VERSION.txt"))) {
            // 加载properties文件
            prop.load(versionTxtFile);

            // 根据key获取value
            String projectName = prop.getProperty("projectName");
            String projectVersion = prop.getProperty("projectVersion");
            String buildTime = prop.getProperty("buildTime");
            SERVER_VERSION = (projectName + " " + projectVersion + " " + buildTime).getBytes();
        } catch (IOException ex) {
            LOGGER.error("遇到异常:", ex);
        }
    }


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
