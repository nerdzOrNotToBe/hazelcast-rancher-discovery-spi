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
import com.hazelcast.config.properties.PropertyTypeConverter;
import com.hazelcast.config.properties.SimplePropertyDefinition;

public class RancherDiscoveryConfiguration {

    public static final PropertyDefinition CLUSTER_NAME = new SimplePropertyDefinition("cluster-name", PropertyTypeConverter.STRING);

    public static final PropertyDefinition STACK_NAME = new SimplePropertyDefinition("stack-name", PropertyTypeConverter.STRING);

    public static final PropertyDefinition ENVIRONMENT_NAME = new SimplePropertyDefinition("environment-name", PropertyTypeConverter.STRING);

    public static final PropertyDefinition RANCHER_API = new SimplePropertyDefinition("rancher-api", PropertyTypeConverter.STRING);

    private RancherDiscoveryConfiguration() {

    }
}
