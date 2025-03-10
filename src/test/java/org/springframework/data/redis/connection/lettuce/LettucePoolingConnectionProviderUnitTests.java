/*
 * Copyright 2019-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.connection.lettuce;

import static org.mockito.Mockito.*;

import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder;

/**
 * Unit tests for {@link LettucePoolingConnectionProvider}.
 *
 * @author Mark Paluch
 * @author Asmir Mustafic
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LettucePoolingConnectionProviderUnitTests {

	@Mock LettuceConnectionProvider connectionProviderMock;
	@Mock StatefulRedisConnection<byte[], byte[]> connectionMock;
	@Mock RedisAsyncCommands<byte[], byte[]> commandsMock;

	private LettucePoolingClientConfiguration config = LettucePoolingClientConfiguration.defaultConfiguration();

	@BeforeEach
	void before() {

		when(connectionMock.async()).thenReturn(commandsMock);
		when(connectionProviderMock.getConnection(any())).thenReturn(connectionMock);
	}

	@Test // DATAREDIS-988
	void shouldReturnConnectionOnRelease() {

		LettucePoolingConnectionProvider provider = new LettucePoolingConnectionProvider(connectionProviderMock, config);

		provider.release(provider.getConnection(StatefulRedisConnection.class));

		verifyNoInteractions(commandsMock);
	}

	@Test // DATAREDIS-988
	void shouldDiscardTransactionOnReleaseOnActiveTransaction() {

		LettucePoolingConnectionProvider provider = new LettucePoolingConnectionProvider(connectionProviderMock, config);
		when(connectionMock.isMulti()).thenReturn(true);

		provider.release(provider.getConnection(StatefulRedisConnection.class));

		verify(commandsMock).discard();
	}

	@Test // GH-3072
	void shouldPrepareThePool() {

		GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
		poolConfig.setMinIdle(5);
		poolConfig.setMaxIdle(8);
		poolConfig.setMaxTotal(10);

		LettucePoolingClientConfiguration config = new LettucePoolingClientConfigurationBuilder().poolConfig(poolConfig)
				.build();

		LettucePoolingConnectionProvider provider = new LettucePoolingConnectionProvider(connectionProviderMock, config);

		provider.getConnection(StatefulRedisConnection.class);
		verify(connectionProviderMock, times(5)).getConnection(any());
	}
}
