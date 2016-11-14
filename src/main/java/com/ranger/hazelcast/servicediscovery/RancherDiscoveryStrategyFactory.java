/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ranger.hazelcast.servicediscovery;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import java.util.*;

/**
 * Created by phaneesh on 01/02/16.
 */
public class RancherDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

	private static final Collection<PropertyDefinition> PROPERTIES =
			Arrays.asList(new PropertyDefinition[]{
					RancherDiscoveryConfiguration.CLUSTER_NAME,
					RancherDiscoveryConfiguration.STACK_NAME,
					RancherDiscoveryConfiguration.ENVIRONMENT_NAME,
					RancherDiscoveryConfiguration.URL,
			});

	@Override
	public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
		return RancherDiscoveryStrategy.class;
	}

	@Override
	public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger,
	                                              Map<String, Comparable> properties) {
		return new RancherDiscoveryStrategy(logger, properties);
	}

	@Override
	public Collection<PropertyDefinition> getConfigurationProperties() {
		return PROPERTIES;
	}
}
