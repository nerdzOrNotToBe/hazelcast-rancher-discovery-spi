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

import com.hazelcast.config.NetworkConfig;
import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by dlebel on 01/02/16.
 */
public class RancherDiscoveryStrategy extends AbstractDiscoveryStrategy {

	public static final String RANCHER_ENVIRONMENT_NAME = "rancher_environment_name";

	private final String clusterName;
	private final String environmentName;
	private final String stackName;
	private final String url;

	private String envId;
	private String stackId;
	private List<String> serviceId = new ArrayList<>();

	RancherDiscoveryStrategy(ILogger logger, Map<String, Comparable> properties) {
		super(logger, properties);

		// make it possible to override the value from the configuration on
		// the system's environment or JVM properties -Ddiscovery.hosts.site-domain=some.domain
		this.clusterName = getOrNull("cluster-name", RancherDiscoveryConfiguration.CLUSTER_NAME);
		this.stackName = getOrNull("stack-name", RancherDiscoveryConfiguration.STACK_NAME);
		this.environmentName = getOrNull("environment-name", RancherDiscoveryConfiguration.ENVIRONMENT_NAME);
		this.url = getOrNull("rancher-api", RancherDiscoveryConfiguration.RANCHER_API);
	}

	@Override
	public Iterable<DiscoveryNode> discoverNodes() {
		CloseableHttpClient client = HttpClients.createDefault();
		List<JSONObject> assignments = null;
		try {
			if(envId == null){
				findEnvironment(client);
			}
			if(stackId == null){
				findStack(client);
			}
			if(envId != null && stackId !=null){
				filterService(client);
				assignments = filterHosts(client);
			}
			client.close();
		} catch (IOException | ParseException e) {
			getLogger().severe(e);
		}
		return mapToDiscoveryNodes(assignments);
	}

	private List<JSONObject> filterHosts(CloseableHttpClient client) throws IOException, ParseException {
		List<JSONObject> list = new ArrayList<>();
		for (String id : serviceId) {
			HttpGet request = new HttpGet(url+"/projects/"+envId+"/services/"+id+"/instances");
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(new InputStreamReader(entity.getContent(), "UTF-8"));
			parseInstancesJson(list, jsonObject);
			EntityUtils.consume(entity);
		}
		return list;
	}

	public static void parseInstancesJson(List<JSONObject> list, JSONObject jsonObject) {
		JSONArray data = (JSONArray) jsonObject.get("data");
		data.forEach(x ->{
			JSONObject env = (JSONObject) x;
			if("running".equals(env.get("state"))) {
				JSONObject assignment = new JSONObject();
				assignment.put("host", env.get("name"));
				assignment.put("ip", env.get("primaryIpAddress"));
				list.add(assignment);
			}
		});
	}

	private void findStack(CloseableHttpClient client) throws IOException, ParseException {
		HttpGet request = new HttpGet(url+"/projects/"+envId+"/environments");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(new InputStreamReader(entity.getContent(), "UTF-8"));
		parseEnvironmentsJson(jsonObject);
		EntityUtils.consume(entity);
	}

	public void parseEnvironmentsJson(JSONObject jsonObject) {
		JSONArray data = (JSONArray) jsonObject.get("data");
		data.forEach(x ->{
			JSONObject stack = (JSONObject) x;
			String name = (String) stack.get("name");
			if(stackName.equals(name)){
				stackId = (String) stack.get("id");
			}
		});
	}

	private void findEnvironment(CloseableHttpClient client) throws IOException, ParseException {
		HttpGet request = new HttpGet(url+"/projects");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(new InputStreamReader(entity.getContent(), "UTF-8"));
		parseProjectsJson(jsonObject);
		EntityUtils.consume(entity);
	}

	public void parseProjectsJson(JSONObject jsonObject) {
		JSONArray data = (JSONArray) jsonObject.get("data");
		data.forEach(x ->{
			JSONObject env = (JSONObject) x;
			String name = (String) env.get("name");
			if(environmentName.equals(name)){
				envId = (String) env.get("id");
			}
		});
	}

	private void filterService(CloseableHttpClient client) throws IOException, ParseException {
		HttpGet request = new HttpGet(url+"/projects/"+envId+"/environments/"+stackId+"/services");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(new InputStreamReader(entity.getContent(), "UTF-8"));
		parseServicesJson(jsonObject);
		EntityUtils.consume(entity);
	}

	public void parseServicesJson(JSONObject jsonObject) {
		JSONArray data = (JSONArray) jsonObject.get("data");
		data.forEach(x ->{
			JSONObject service = (JSONObject) x;
			JSONObject launchConfig = (JSONObject) service.get("launchConfig");
			if(  launchConfig.get("environment") != null) {
				JSONObject environment = (JSONObject) launchConfig.get("environment");
				if (environment.get("cluster-name") != null) {
					String cluster = (String) environment.get("cluster-name");
					if (clusterName.equals(cluster)) {
						serviceId.add((String) service.get("id"));
					}
				}
			}
		});
	}

	private Iterable<DiscoveryNode> mapToDiscoveryNodes(List<JSONObject> assignments) {
		Collection<DiscoveryNode> discoveredNodes = new ArrayList<>();
		if(assignments != null) {
			for (JSONObject assignment : assignments) {
				String address = (String) assignment.get("ip");
				String hostname = (String) assignment.get("host");

				Map<String, Object> attributes = Collections.<String, Object>singletonMap("hostname", hostname);

				InetAddress inetAddress = mapToInetAddress(address);
				Address addr = new Address(inetAddress, NetworkConfig.DEFAULT_PORT);

				discoveredNodes.add(new SimpleDiscoveryNode(addr, attributes));
			}
		}
		return discoveredNodes;
	}


	private InetAddress mapToInetAddress(String address) {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Could not resolve ip address", e);
		}
	}
}
