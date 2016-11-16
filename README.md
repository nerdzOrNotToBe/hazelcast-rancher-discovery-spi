# hazelcast-rancher-discovery-spi
Rancher based discovery strategy SPI for Hazelcast enabled applications

This is a discovery strategy extension for Hazelcast to make discovery work on [rancher](http://rancher.com/rancher/).
This library compiles only on Java 8.
 

## Usage
Hazelcast Rancher Discovery provides a easy way to enable member discovery with elastic applications on docker like environment where using a static host list or using multicast based discovery is not possible.
 
### Build instructions
  - Clone the source:

        git clone https://github.com/nerdzOrNotToBe/hazelcast-rancher-discovery-spi.git

  - Build

        mvn install

### Maven Dependency
Use the following maven dependency:
```xml
<dependency>
    <groupId>com.spotter</groupId>
    <artifactId>hazelcast-rancher-discovery-spi</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Using Hazelcast Ranger Discovery
```java
Config config = new Config();
//This is important to enable the discovery strategy
config.setProperty(GroupProperty.DISCOVERY_SPI_ENABLED, "true");
config.setProperty(GroupProperty.DISCOVERY_SPI_PUBLIC_IP_ENABLED, "true");
config.setProperty(GroupProperty.SOCKET_CLIENT_BIND_ANY, "false");
config.setProperty(GroupProperty.SOCKET_BIND_ANY, "false");
NetworkConfig networkConfig = config.getNetworkConfig();
JoinConfig joinConfig = networkConfig.getJoin();
joinConfig.getTcpIpConfig().setEnabled(false);
joinConfig.getMulticastConfig().setEnabled(false);
joinConfig.getAwsConfig().setEnabled(false);
DiscoveryConfig discoveryConfig = joinConfig.getDiscoveryConfig();
//Set the discovery strategy to RancherDiscoveryStrategy
DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(new RancherDiscoveryStrategyFactory());
discoveryStrategyConfig.addProperty("cluster-name", "test");
discoveryStrategyConfig.addProperty("stack-name", "test");
discoveryStrategyConfig.addProperty("environment-name", "Default");
discoveryStrategyConfig.addProperty("rancher-api", "http://localhost:8080/v1");
discoveryConfig.addDiscoveryStrategyConfig(discoveryStrategyConfig);
//Create the hazelcast instance
HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);
```

LICENSE
-------

Copyright 2016 Dimitri Lebel <lebel.dimitri@gmail.com>.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
