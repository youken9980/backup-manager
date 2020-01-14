package com.youken.backupmanager.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 杨剑
 * @date 2020/1/8
 */
@Component
public class RedisConfig {

	private static boolean usePool;
	private static int database;
	private static String host;
	private static int port;
	private static String password;
	private static String clusterNodes;
	private static int maxRedirects;
	private static long commandTimeout;
	private static boolean testOnBorrow;
	private static int maxActive;
	private static long maxWait;
	private static int maxIdle;
	private static int minIdle;

	@Value("${spring.redis.usePool}")
	public void setUsePool(boolean usePool) {
		RedisConfig.usePool = usePool;
	}

	@Value("${spring.redis.database}")
	public void setDatabase(int database) {
		RedisConfig.database = database;
	}

	@Value("${spring.redis.host}")
	public void setHost(String host) {
		RedisConfig.host = host;
	}

	@Value("${spring.redis.port}")
	public void setPort(int port) {
		RedisConfig.port = port;
	}

	@Value("${spring.redis.password}")
	public void setPassword(String password) {
		RedisConfig.password = password;
	}

	@Value("${spring.redis.cluster.nodes}")
	public void setClusterNodes(String clusterNodes) {
		RedisConfig.clusterNodes = clusterNodes;
	}

	@Value("${spring.redis.cluster.maxRedirects}")
	public void setMaxRedirects(int maxRedirects) {
		RedisConfig.maxRedirects = maxRedirects;
	}

	@Value("${spring.redis.commandTimeout}")
	public void setCommandTimeout(long commandTimeout) {
		RedisConfig.commandTimeout = commandTimeout;
	}

	@Value("${spring.redis.testOnBorrow}")
	public void setTestOnBorrow(boolean testOnBorrow) {
		RedisConfig.testOnBorrow = testOnBorrow;
	}

	@Value("${spring.redis.lettuce.pool.maxActive}")
	public void setMaxActive(int maxActive) {
		RedisConfig.maxActive = maxActive;
	}

	@Value("${spring.redis.lettuce.pool.maxWait}")
	public void setMaxWait(long maxWait) {
		RedisConfig.maxWait = maxWait;
	}

	@Value("${spring.redis.lettuce.pool.maxIdle}")
	public void setMaxIdle(int maxIdle) {
		RedisConfig.maxIdle = maxIdle;
	}

	@Value("${spring.redis.lettuce.pool.minIdle}")
	public void setMinIdle(int minIdle) {
		RedisConfig.minIdle = minIdle;
	}

	@Bean
	public RedisTemplate<Serializable, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
		RedisTemplate<Serializable, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(lettuceConnectionFactory);
		return template;
	}

	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory(GenericObjectPoolConfig genericObjectPoolConfig) {
		LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
				.poolConfig(genericObjectPoolConfig)
				.commandTimeout(Duration.ofMillis(commandTimeout))
				.build();
		return new LettuceConnectionFactory(usePool ? getClusterConfig() : getStandaloneConfig(), clientConfig);
	}

	/**
	 * GenericObjectPoolConfig 连接池配置
	 */
	@Bean
	public GenericObjectPoolConfig genericObjectPoolConfig() {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(maxActive);
		poolConfig.setMaxWaitMillis(maxWait);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setTestOnBorrow(testOnBorrow);
		return poolConfig;
	}

	/**
	 * 集群配置
	 */
	private RedisClusterConfiguration getClusterConfig() {
		RedisClusterConfiguration config = new RedisClusterConfiguration();
		String[] serverArray = clusterNodes.split(",");
		Set<RedisNode> nodes = new HashSet<>();
		for (String ipPort : serverArray) {
			String[] ipAndPort = ipPort.split(":");
			nodes.add(new RedisNode(ipAndPort[0].trim(), Integer.parseInt(ipAndPort[1])));
		}
		config.setClusterNodes(nodes);
		config.setMaxRedirects(maxRedirects);
		return config;
	}

	/**
	 * 单机配置
	 */
	private RedisStandaloneConfiguration getStandaloneConfig() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		config.setDatabase(database);
		config.setHostName(host);
		config.setPort(port);
		config.setPassword(RedisPassword.of(password));
		return config;
	}
}
