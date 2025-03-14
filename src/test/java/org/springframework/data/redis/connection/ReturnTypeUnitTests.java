/*
 * Copyright 2020-2025 the original author or authors.
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
package org.springframework.data.redis.connection;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link ReturnType}.
 *
 * @author Mark Paluch
 */
class ReturnTypeUnitTests {

	@ParameterizedTest // DATAREDIS-1245
	@ValueSource(classes = { List.class, ArrayList.class, LinkedList.class })
	void shouldConsiderListsAsMultiType(Class<?> listClass) {
		assertThat(ReturnType.fromJavaType(listClass)).isEqualTo(ReturnType.MULTI);
	}

	@ParameterizedTest // GH-3090
	@ValueSource(classes = { Integer.class, Long.class, Number.class })
	void shouldConsiderIntegerType(Class<?> listClass) {
		assertThat(ReturnType.fromJavaType(listClass)).isEqualTo(ReturnType.INTEGER);
	}

	@ParameterizedTest // GH-3090
	@ValueSource(classes = { Double.class, Float.class, String.class })
	void shouldConsiderValueType(Class<?> listClass) {
		assertThat(ReturnType.fromJavaType(listClass)).isEqualTo(ReturnType.VALUE);
	}

}
