
package io.shardingcat;

import org.junit.Test;

import io.shardingcat.config.ConfigInitializer;

/**
 * @author shardingcat
 */
public class ConfigInitializerTest {
    @Test
    public void testConfigLoader() {
        new ConfigInitializer(true);
    }
}