/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v10.html
 */
package io.lighty.modules.northbound.restconf.community.impl.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import io.lighty.core.controller.api.LightyServices;
import io.lighty.core.controller.impl.config.ConfigurationException;
import io.lighty.modules.northbound.restconf.community.impl.config.RestConfConfiguration;
import java.io.IOException;
import java.io.InputStream;

import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class RestConfConfigUtils {

    private static final Logger LOG = LoggerFactory.getLogger(RestConfConfigUtils.class);
    public static final String RESTCONF_CONFIG_ROOT_ELEMENT_NAME = "restconf";
    public static final Set<YangModuleInfo> YANG_MODELS = ImmutableSet.of(
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.$YangModuleInfoImpl.getInstance(),
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.monitoring.rev170126.$YangModuleInfoImpl.getInstance()
    );

    private RestConfConfigUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Load restconf configuration from InputStream containing JSON data.
     * @param jsonConfigInputStream
     *   InputStream containing RestConf configuration data in JSON format.
     * @return
     *   Object representation of configuration data.
     * @throws ConfigurationException
     *   In case InputStream does not contain valid JSON data or cannot bind Json tree to type.
     */
    public static RestConfConfiguration getRestConfConfiguration(InputStream jsonConfigInputStream)
            throws ConfigurationException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode configNode;
        try {
            configNode = mapper.readTree(jsonConfigInputStream);
        } catch (IOException e) {
            throw new ConfigurationException("Cannot deserialize Json content to Json tree nodes", e);
        }
        if (!configNode.has(RESTCONF_CONFIG_ROOT_ELEMENT_NAME)) {
            LOG.warn("Json config does not contain {} element. Using defaults.", RESTCONF_CONFIG_ROOT_ELEMENT_NAME);
            return new RestConfConfiguration();
        }
        JsonNode restconfNode = configNode.path(RESTCONF_CONFIG_ROOT_ELEMENT_NAME);
        RestConfConfiguration restconfConfiguration = null;
        try {
            restconfConfiguration = mapper.treeToValue(restconfNode, RestConfConfiguration.class);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException(String.format("Cannot bind Json tree to type: %s",
                    RestConfConfiguration.class), e);
        }

        return restconfConfiguration;
    }

    /**
     * Load restconf configuration from InputStream containing JSON data and use lightyServices to
     * get references to necessary Lighty services.
     * @param jsonConfigInputStream
     *   InputStream containing RestConf configuration data in JSON format.
     * @param lightyServices
     *   This object instace contains references to initialized Lighty services required for RestConf.
     * @return
     *   Object representation of configuration data.
     * @throws ConfigurationException
     *   In case InputStream does not contain valid JSON data or cannot bind Json tree to type.
     */
    public static RestConfConfiguration getRestConfConfiguration(final InputStream jsonConfigInputStream,
            final LightyServices lightyServices) throws ConfigurationException {
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode configNode;
        try {
            configNode = mapper.readTree(jsonConfigInputStream);
        } catch (IOException e) {
            throw new ConfigurationException("Cannot deserialize Json content to Json tree nodes", e);
        }
        if (!configNode.has(RESTCONF_CONFIG_ROOT_ELEMENT_NAME)) {
            LOG.warn("Json config does not contain {} element. Using defaults.", RESTCONF_CONFIG_ROOT_ELEMENT_NAME);
            return getDefaultRestConfConfiguration(lightyServices);
        }
        JsonNode restconfNode = configNode.path(RESTCONF_CONFIG_ROOT_ELEMENT_NAME);

        RestConfConfiguration restconfConfiguration = null;
        try {
            restconfConfiguration = mapper.treeToValue(restconfNode, RestConfConfiguration.class);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException(String.format("Cannot bind Json tree to type: %s",
                    RestConfConfiguration.class), e);
        }
        restconfConfiguration.setDomDataBroker(lightyServices.getClusteredDOMDataBroker());
        restconfConfiguration.setSchemaService(lightyServices.getSchemaService());
        restconfConfiguration.setDomRpcService(lightyServices.getDOMRpcService());
        restconfConfiguration.setDomNotificationService(lightyServices.getDOMNotificationService());
        restconfConfiguration.setDomMountPointService(lightyServices.getDOMMountPointService());
        restconfConfiguration.setDomSchemaService(lightyServices.getDOMSchemaService());

        return restconfConfiguration;
    }

    /**
     * Get default RestConf configuration using provided Lighty services.
     * @param lightyServices
     *   This object instace contains references to initialized Lighty services required for RestConf.
     * @return
     *   Object representation of configuration data.
     */
    public static RestConfConfiguration getDefaultRestConfConfiguration(final LightyServices lightyServices) {
        return new RestConfConfiguration(
                lightyServices.getClusteredDOMDataBroker(),
                lightyServices.getSchemaService(),
                lightyServices.getDOMRpcService(),
                lightyServices.getDOMNotificationService(),
                lightyServices.getDOMMountPointService(),
                lightyServices.getDOMSchemaService());
    }

    /**
     * Get default RestConf configuration, Lighty services are not populated in this configuration.
     * @return
     *   Object representation of configuration data.
     */
    public static RestConfConfiguration getDefaultRestConfConfiguration() {
        return new RestConfConfiguration();
    }

    /**
     * Copy existing RestConf configuration and use provided lightyServices
     * to populate references to necessary Lighty services.
     * @param restConfConfiguration
     *   Object representation of configuration data.
     * @param lightyServices
     *   This object instace contains references to initialized Lighty services required for RestConf.
     * @return
     *   Object representation of configuration data.
     */
    public static RestConfConfiguration getRestConfConfiguration(final RestConfConfiguration restConfConfiguration,
                                                                 final LightyServices lightyServices) {
        final RestConfConfiguration config = new RestConfConfiguration(restConfConfiguration);
        config.setDomDataBroker(lightyServices.getClusteredDOMDataBroker());
        config.setSchemaService(lightyServices.getSchemaService());
        config.setDomRpcService(lightyServices.getDOMRpcService());
        config.setDomNotificationService(lightyServices.getDOMNotificationService());
        config.setDomMountPointService(lightyServices.getDOMMountPointService());
        config.setDomSchemaService(lightyServices.getDOMSchemaService());
        return config;
    }


}
