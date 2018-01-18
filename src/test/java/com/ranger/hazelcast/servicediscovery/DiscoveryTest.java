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

import com.hazelcast.spi.discovery.DiscoveryNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class DiscoveryTest {

	@Test
	@Ignore
	public void testDiscovery() throws IOException, ParseException {
		Map<String, Comparable> properties = new HashMap<>();
		properties.put("cluster-name", "backend");
		properties.put("stack-name", "vertx1");
		properties.put("environment-name", "Default");
		properties.put("rancher-api", "http://10.34.0.252:8080/v1");
		RancherDiscoveryStrategy rancherDiscoveryStrategy = new RancherDiscoveryStrategy(null, properties);
		JSONObject projects = (JSONObject) new JSONParser().parse(new FileReader( getClass().getClassLoader().getResource("projects.json").getFile()));
		JSONObject environments = (JSONObject) new JSONParser().parse(new FileReader( getClass().getClassLoader().getResource("environments.json").getFile()));
		JSONObject services = (JSONObject) new JSONParser().parse(new FileReader( getClass().getClassLoader().getResource("services.json").getFile()));
		JSONObject instances = (JSONObject) new JSONParser().parse(new FileReader( getClass().getClassLoader().getResource("instances.json").getFile()));
		rancherDiscoveryStrategy.parseProjectsJson(projects);
		rancherDiscoveryStrategy.parseEnvironmentsJson(environments);
		rancherDiscoveryStrategy.parseServicesJson(services);
		ArrayList<JSONObject> list = new ArrayList<>();
		rancherDiscoveryStrategy.parseInstancesJson(list,instances);
		assertEquals(list.size(),4);
	}

	@Test
	@Ignore
	public void testDiscovery2() throws IOException, ParseException {
		Map<String, Comparable> properties = new HashMap<>();
		properties.put("cluster-name", "backend");
		properties.put("stack-name", "spotter2");
		properties.put("environment-name", "Default");
		properties.put("rancher-api", "http://10.93.241.241:8080/v1");
		RancherDiscoveryStrategy rancherDiscoveryStrategy = new RancherDiscoveryStrategy(null, properties);
		Iterable<DiscoveryNode> discoveryNodes = rancherDiscoveryStrategy.discoverNodes();
		assertEquals(discoveryNodes,0);
	}

}
