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

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.GroupProperty;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertTrue;


public class DiscoveryTest {


    @Test
    @Ignore
    public void testSingleMemberDiscovery() throws IOException {
        HazelcastInstance hazelcast = getHazelcastInstance(5701);
        assertTrue(hazelcast.getCluster().getMembers().size() > 0);
        hazelcast.shutdown();
    }

    @Test
    @Ignore
    public void testMultiMemberDiscovery() throws UnknownHostException {
        HazelcastInstance hazelcast1 = getHazelcastInstance(5701);
        HazelcastInstance hazelcast2 = getHazelcastInstance(5801);
        HazelcastInstance hazelcast3 = getHazelcastInstance(5901);
        assertTrue(hazelcast3.getCluster().getMembers().size() > 0);
        assertTrue(hazelcast3.getCluster().getMembers().size() == 3);
        hazelcast1.shutdown();
        hazelcast2.shutdown();
        hazelcast3.shutdown();
    }

    private HazelcastInstance getHazelcastInstance(int port) throws UnknownHostException {
        Config config = new Config();
        config.setProperty(GroupProperty.DISCOVERY_SPI_ENABLED, "true");
        config.setProperty(GroupProperty.DISCOVERY_SPI_PUBLIC_IP_ENABLED, "true");
        config.setProperty(GroupProperty.SOCKET_CLIENT_BIND_ANY, "false");
        config.setProperty(GroupProperty.SOCKET_BIND_ANY, "false");
        NetworkConfig networkConfig = config.getNetworkConfig();
	    networkConfig.setInterfaces(new InterfacesConfig().addInterface("10.34.*.*").setEnabled(true));
        networkConfig.getInterfaces().addInterface(InetAddress.getLocalHost().getHostAddress()).setEnabled(true);
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getTcpIpConfig().setEnabled(false);
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getAwsConfig().setEnabled(false);
        DiscoveryConfig discoveryConfig = joinConfig.getDiscoveryConfig();
        DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(new RancherDiscoveryStrategyFactory());
        discoveryStrategyConfig.addProperty("cluster-name", "pulse");
        discoveryStrategyConfig.addProperty("stack-name", "vertx1");
        discoveryStrategyConfig.addProperty("environment-name", "Default");
        discoveryStrategyConfig.addProperty("url", "http://10.34.0.252:8080/v1");
        discoveryConfig.addDiscoveryStrategyConfig(discoveryStrategyConfig);
        return Hazelcast.newHazelcastInstance(config);
    }
}
