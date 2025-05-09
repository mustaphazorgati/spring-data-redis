/*
 * Copyright 2011-2025 the original author or authors.
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
package org.springframework.data.redis.support.collections;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * @author Costin Leau
 */
class SupportXmlIntegrationTests {

	@Test
	void testContainerSetup() throws Exception {
		GenericXmlApplicationContext ctx = new GenericXmlApplicationContext(
				"/org/springframework/data/redis/support/collections/container.xml");

		RedisList list = ctx.getBean("non-existing", RedisList.class);
		RedisProperties props = ctx.getBean("props", RedisProperties.class);
		Map map = ctx.getBean("map", Map.class);

		ctx.close();
	}
}
