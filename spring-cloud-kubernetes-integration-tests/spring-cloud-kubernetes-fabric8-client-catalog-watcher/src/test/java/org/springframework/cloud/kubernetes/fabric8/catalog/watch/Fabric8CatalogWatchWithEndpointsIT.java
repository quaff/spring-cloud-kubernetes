/*
 * Copyright 2012-2024 the original author or authors.
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

package org.springframework.cloud.kubernetes.fabric8.catalog.watch;

import java.util.Set;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;
import org.springframework.cloud.kubernetes.fabric8.discovery.KubernetesCatalogWatchAutoConfiguration;
import org.springframework.cloud.kubernetes.integration.tests.commons.Images;
import org.springframework.cloud.kubernetes.integration.tests.commons.Phase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.springframework.cloud.kubernetes.fabric8.catalog.watch.Fabric8CatalogWatchWithEndpointsIT.TestConfig;
import static org.springframework.cloud.kubernetes.fabric8.catalog.watch.TestAssertions.assertLogStatement;
import static org.springframework.cloud.kubernetes.fabric8.catalog.watch.TestAssertions.invokeAndAssert;

/**
 * @author wind57
 */
@SpringBootTest(classes = { KubernetesCatalogWatchAutoConfiguration.class, TestConfig.class, Application.class },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Fabric8CatalogWatchWithEndpointsIT extends Fabric8CatalogWatchBase {

	@LocalServerPort
	private int port;

	@BeforeEach
	void beforeEach() {
		Images.loadBusybox(K3S);
		util.busybox(NAMESPACE, Phase.CREATE);
	}

	@AfterEach
	void afterEach() {
		// empty on purpose
		// busybox is deleted as part of the test itself, thus not seen here
	}

	/**
	 * <pre>
	 *     - we deploy a busybox service with 2 replica pods
	 *     - we use endpoints
	 *     - we receive an event from KubernetesCatalogWatcher, assert what is inside it
	 *     - delete the busybox service
	 *     - assert that we receive an empty response
	 * </pre>
	 */
	@Test
	void test(CapturedOutput output) {
		assertLogStatement(output, "stateGenerator is of type: Fabric8EndpointsCatalogWatch");
		invokeAndAssert(util, Set.of(NAMESPACE), port, NAMESPACE);
	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		@Primary
		KubernetesClient kubernetesClient() {
			return client();
		}

		@Bean
		@Primary
		KubernetesDiscoveryProperties kubernetesDiscoveryProperties() {
			return discoveryProperties(false);
		}

	}

}
