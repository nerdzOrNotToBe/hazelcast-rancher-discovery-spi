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
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by dlebel on 01/02/16.
 */
public class RancherDiscoveryStrategy extends AbstractDiscoveryStrategy {

	public static final String RANCHER_ENVIRONMENT_NAME = "rancher_environment_name";

	private final String siteDomain;

	RancherDiscoveryStrategy(ILogger logger, Map<String, Comparable> properties) {
		super(logger, properties);

		// make it possible to override the value from the configuration on
		// the system's environment or JVM properties -Ddiscovery.hosts.site-domain=some.domain
		this.siteDomain = getOrNull("discovery.hosts", RancherDiscoveryConfiguration.SERVICE_NAME);
	}

	@Override
	public Iterable<DiscoveryNode> discoverNodes() {
		List<String> assignments = filterHosts();
		return mapToDiscoveryNodes(assignments);
	}

	private List<String> filterHosts() {
		String environment_name = System.getenv(RANCHER_ENVIRONMENT_NAME);
		try {
			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(RancherDiscoveryConfiguration.URL.key());
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(new InputStreamReader(entity.getContent(), "UTF-8"));

		} catch (IOException e) {
			getLogger().severe(e);
		} catch (ParseException e) {
			getLogger().severe(e);
		}
		List<String> assignments = new ArrayList<>();
		return assignments;
	}

	private Iterable<DiscoveryNode> mapToDiscoveryNodes(List<String> assignments) {
		Collection<DiscoveryNode> discoveredNodes = new ArrayList<>();

		for (String assignment : assignments) {
			String address = sliceAddress(assignment);
			String hostname = sliceHostname(assignment);

			Map<String, Object> attributes = Collections.<String, Object>singletonMap("hostname", hostname);

			InetAddress inetAddress = mapToInetAddress(address);
			Address addr = new Address(inetAddress, NetworkConfig.DEFAULT_PORT);

			discoveredNodes.add(new SimpleDiscoveryNode(addr, attributes));
		}
		return discoveredNodes;
	}

	private List<String> readLines(File hosts) {
		try {
			List<String> lines = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(hosts));

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.startsWith("#")) {
					lines.add(line.trim());
				}
			}

			return lines;
		} catch (IOException e) {
			throw new RuntimeException("Could not read hosts file", e);
		}
	}

	private boolean matchesDomain(String line) {
		if (line.isEmpty()) {
			return false;
		}
		String hostname = sliceHostname(line);
		return hostname.endsWith("." + siteDomain);
	}

	private String sliceAddress(String assignment) {
		String[] tokens = assignment.split("\\p{javaSpaceChar}+");
		if (tokens.length < 1) {
			throw new RuntimeException("Could not find ip address in " + assignment);
		}
		return tokens[0];
	}

	private static String sliceHostname(String assignment) {
		String[] tokens = assignment.split("(\\p{javaSpaceChar}+|\t+)+");
		if (tokens.length < 2) {
			throw new RuntimeException("Could not find hostname in " + assignment);
		}
		return tokens[1];
	}

	private InetAddress mapToInetAddress(String address) {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Could not resolve ip address", e);
		}
	}
}