/*
 * Copyright 2026-present the original author or authors.
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
package org.springframework.data.redis.core;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultReactiveListOperations}.
 *
 * @author Chris Bono
 */
class DefaultReactiveListOperationsUnitTests {

	@Nested // GH-2975
	class OperationsWithTimeoutShould {

		private DefaultReactiveListOperations<String, String> listOperations = new DefaultReactiveListOperations<>(mock(), mock());

		@ParameterizedTest
		@DisplayName("CallTimeoutUtil")
		@MethodSource("operationsWithTimeout")
		void callTimeoutUtil(BiConsumer<DefaultReactiveListOperations<String, String>, Duration> operationWithTimeout) {

			try (MockedStatic<DefaultReactiveListOperations> listOperationsMockedStatic = Mockito.mockStatic(
					DefaultReactiveListOperations.class)) {
				listOperationsMockedStatic.when(() -> DefaultReactiveListOperations.isZeroOrGreaterOneSecond(any())).thenCallRealMethod();
				operationWithTimeout.accept(listOperations, Duration.ofSeconds(1));
				listOperationsMockedStatic.verify(() -> DefaultReactiveListOperations.isZeroOrGreaterOneSecond(Duration.ofSeconds(1)));
			}
		}

		@ParameterizedTest
		@DisplayName("NotFailWithValidTimeout")
		@MethodSource("operationsWithTimeout")
		void notFailWithValidTimeout(BiConsumer<DefaultReactiveListOperations<String, String>, Duration> operationWithTimeout) {
			assertThatNoException().isThrownBy(() -> operationWithTimeout.accept(listOperations, Duration.ofSeconds(1)));
		}

		@ParameterizedTest
		@DisplayName("FailWithInvalidTimeout")
		@MethodSource("operationsWithTimeout")
		void failWithInvalidTimeout(BiConsumer<DefaultReactiveListOperations<String, String>, Duration> operationWithTimeout) {
			assertThatIllegalArgumentException().isThrownBy(() -> operationWithTimeout.accept(listOperations, Duration.ofMillis(500)))
					.withMessage("Duration must be either zero or greater or equal to 1 second");
		}

		static Stream<Arguments> operationsWithTimeout() {
			return Stream.of(
					argumentSet("forLeftPop",
							(BiConsumer<DefaultReactiveListOperations<String, String>, Duration>) (listOps, timeout) -> listOps.leftPop("someKey", timeout)),
					argumentSet("forRightPop",
							(BiConsumer<DefaultReactiveListOperations<String, String>, Duration>) (listOps, timeout) -> listOps.rightPop("someKey", timeout)),
					argumentSet("forRightPopAndLeftPush",
							(BiConsumer<DefaultReactiveListOperations<String, String>, Duration>) (listOps, timeout) -> listOps.rightPopAndLeftPush("someKey", "someOtherKey", timeout))
			);
		}
	}

	@Nested // GH-2975
	class ZeroOrGreaterThanOneSecondShould {

		@ParameterizedTest
		@DisplayName("ReturnTrueWhenTimeoutIs")
		@MethodSource
		void returnTrueWhenTimeoutIs(Duration timeout) {
			assertThat(DefaultReactiveListOperations.isZeroOrGreaterOneSecond(timeout)).isTrue();
		}

		static Stream<Arguments> returnTrueWhenTimeoutIs() {
			return Stream.of(
					argumentSet("zero", Duration.ZERO),
					argumentSet("oneSecond", Duration.ofSeconds(1)),
					argumentSet("greaterThanOneSecond", Duration.ofMillis(1001)),
					argumentSet("greaterThanOneSecondIncludingNano", Duration.ofSeconds(1).plusNanos(500_000_000))
			);
		}

		@Test
		void returnFalseWhenTimeoutIsLessThanOneSecond() {
			assertThat(DefaultReactiveListOperations.isZeroOrGreaterOneSecond(Duration.ofMillis(500))).isFalse();
		}

	}
}
