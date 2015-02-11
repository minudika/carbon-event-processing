/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.output.adaptor.wso2event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.event.notifier.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.notifier.core.MessageType;
import org.wso2.carbon.event.notifier.core.Property;
import org.wso2.carbon.event.notifier.core.config.EndpointAdaptorConfiguration;
import org.wso2.carbon.event.notifier.core.exception.EndpointAdaptorProcessingException;
import org.wso2.carbon.event.output.adaptor.wso2event.internal.ds.WSO2EventAdaptorServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.wso2event.internal.util.WSO2EventAdaptorConstants;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class WSO2EventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(WSO2EventAdaptorType.class);
    private static WSO2EventAdaptorType wso2EventAdaptor = new WSO2EventAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<EndpointAdaptorConfiguration, LoadBalancingDataPublisher>> dataPublisherMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<EndpointAdaptorConfiguration, LoadBalancingDataPublisher>>();
    private Agent agent;

    private WSO2EventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.WSO2EVENT);

        return supportOutputMessageTypes;
    }

    /**
     * @return WSO2Event adaptor instance
     */
    public static WSO2EventAdaptorType getInstance() {

        return wso2EventAdaptor;
    }

    /**
     * @return name of the WSO2Event adaptor
     */
    @Override
    protected String getName() {
        return WSO2EventAdaptorConstants.ADAPTOR_TYPE_WSO2EVENT;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.wso2event.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set receiver url event adaptor
        Property ipProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_RECEIVER_URL);
        ipProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_RECEIVER_URL));
        ipProperty.setRequired(true);
        ipProperty.setHint(resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_HINT_RECEIVER_URL));

        // set authenticator url of event adaptor
        Property authenticatorIpProperty = new Property(WSO2EventAdaptorConstants.
                ADAPTOR_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL);
        authenticatorIpProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL));
        authenticatorIpProperty.setRequired(false);
        authenticatorIpProperty.setHint(resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_HINT_AUTHENTICATOR_URL));

        // set connection user name as property
        Property userNameProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_USER_NAME);
        userNameProperty.setRequired(true);
        userNameProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_USER_NAME));
        userNameProperty.setHint(resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_HINT_USER_NAME));

        // set connection password as property
        Property passwordProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_PASSWORD);
        passwordProperty.setRequired(true);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_PASSWORD));
        passwordProperty.setHint(resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_HINT_PASSWORD));

        // set stream definition
        Property streamDefinitionProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_NAME);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_NAME));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setDefaultValue("1.0.0");
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);
        propertyList.add(ipProperty);
        propertyList.add(authenticatorIpProperty);
        propertyList.add(userNameProperty);
        propertyList.add(passwordProperty);

        return propertyList;

    }


    /**
     * @param message  - is and Object[]{Event, EventDefinition}
     * @param endpointAdaptorConfiguration
     *
     * @param tenantId
     */
    public void publish(
            Object message,
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {
        ConcurrentHashMap<EndpointAdaptorConfiguration, LoadBalancingDataPublisher> dataPublishers = dataPublisherMap.get(tenantId);
        if (dataPublishers == null) {
            dataPublishers = new ConcurrentHashMap<EndpointAdaptorConfiguration, LoadBalancingDataPublisher>();
            dataPublisherMap.putIfAbsent(tenantId, dataPublishers);
            dataPublishers = dataPublisherMap.get(tenantId);
        }
        LoadBalancingDataPublisher dataPublisher = dataPublishers.get(endpointAdaptorConfiguration);
        if (dataPublisher == null) {
            synchronized (this) {
                dataPublisher = dataPublishers.get(endpointAdaptorConfiguration);
                if (dataPublisher == null) {
                    dataPublisher = createDataPublisher(endpointAdaptorConfiguration);
                    dataPublishers.putIfAbsent(endpointAdaptorConfiguration, dataPublisher);
                }
            }
        }

        try {
            Event event = (Event) ((Object[]) message)[0];
            StreamDefinition streamDefinition = (StreamDefinition) ((Object[]) message)[1];

            if (!dataPublisher.isStreamDefinitionAdded(streamDefinition)) {
                dataPublisher.addStreamDefinition(streamDefinition);

                //Sending the first Event
                publishEvent(endpointAdaptorConfiguration, dataPublisher, event, streamDefinition);
            } else {
                //Sending Events
                publishEvent(endpointAdaptorConfiguration, dataPublisher, event, streamDefinition);
            }
        } catch (Exception ex) {
            throw new EndpointAdaptorProcessingException(
                    ex.getMessage() + " Error Occurred When Publishing Events", ex);
        }

    }

    private LoadBalancingDataPublisher createDataPublisher(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration)
            throws EndpointAdaptorProcessingException {
        if (agent == null) {
            agent = WSO2EventAdaptorServiceValueHolder.getAgent();
        }

        Map<String, String> adaptorOutputProperties = endpointAdaptorConfiguration.getOutputAdaptorProperties();
        String userName = adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_USER_NAME);
        String password = adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_PASSWORD);

        if (null != adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL) && adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL).length() > 0) {

            ArrayList<String> authenticatorGroupUrls = DataPublisherUtil.getReceiverGroups(adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL));
            ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_RECEIVER_URL));

            if (authenticatorGroupUrls.size() != receiverGroupUrls.size()) {
                throw new EndpointAdaptorProcessingException("Receiver group URLs are not equal to the Authenticator group URLs, Receiver group URLs:" + adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_RECEIVER_URL) + " & Authenticator group URLs :" + adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL));
            }
            ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
            for (int i = 0; i < receiverGroupUrls.size(); i++) {
                String aReceiverGroupURL = receiverGroupUrls.get(i);
                String aAuthenticatorGroupURL = authenticatorGroupUrls.get(i);
                ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                String[] receiverUrls = aReceiverGroupURL.split(",");
                String[] authenticatorUrls = aAuthenticatorGroupURL.split(",");
                if (receiverUrls.length != authenticatorUrls.length) {
                    throw new EndpointAdaptorProcessingException("Receiver URLs are not equal to the Authenticator URLs, on Receiver group:" + aReceiverGroupURL + " & Authenticator group:" + aAuthenticatorGroupURL);
                }
                for (int i1 = 0, receiverUrlsLength = receiverUrls.length; i1 < receiverUrlsLength; i1++) {
                    String receiverUrl = receiverUrls[i1];
                    String authenticatorUrl = authenticatorUrls[i1];
                    DataPublisherHolder aNode = new DataPublisherHolder(authenticatorUrl.trim(), receiverUrl.trim(), userName,
                            password);
                    dataPublisherHolders.add(aNode);
                }
                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                allReceiverGroups.add(group);
            }

            return new LoadBalancingDataPublisher(allReceiverGroups);

        } else {
            ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(adaptorOutputProperties.get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_RECEIVER_URL));

            ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
            for (String aReceiverGroupURL : receiverGroupUrls) {
                ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                String[] receiverUrls = aReceiverGroupURL.split(",");
                for (String receiverUrl : receiverUrls) {
                    DataPublisherHolder aNode = new DataPublisherHolder(null, receiverUrl.trim(), userName,
                            password);
                    dataPublisherHolders.add(aNode);
                }
                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                allReceiverGroups.add(group);
            }

            return new LoadBalancingDataPublisher(allReceiverGroups);
        }
    }

    private void publishEvent(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration,
            LoadBalancingDataPublisher dataPublisher,
            Event event, StreamDefinition streamDefinition) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("At publisher of the Output WSO2Event Adaptor " + event);
            }
            dataPublisher.publish(streamDefinition.getName(), streamDefinition.getVersion(), event);
        } catch (AgentException ex) {
            throw new EndpointAdaptorProcessingException(
                    "Cannot publish data via DataPublisher for the adaptor configuration:" +
                            endpointAdaptorConfiguration.getEndpointAdaptorName() + " for the  event " + event, ex);
        }

    }

    @Override
    public void testConnection(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {
        try {
            DataPublisher dataPublisher = new DataPublisher(endpointAdaptorConfiguration.getOutputAdaptorProperties().get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_RECEIVER_URL), endpointAdaptorConfiguration.getOutputAdaptorProperties().get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_USER_NAME), endpointAdaptorConfiguration.getOutputAdaptorProperties().get(WSO2EventAdaptorConstants.ADAPTOR_CONF_WSO2EVENT_PROP_PASSWORD));
            dataPublisher.findStreamId("TestStream", "1.0.0");
        } catch (MalformedURLException e) {
            throw new EndpointAdaptorProcessingException(e);
        } catch (AgentException e) {
            throw new EndpointAdaptorProcessingException(e);
        } catch (AuthenticationException e) {
            throw new EndpointAdaptorProcessingException(e);
        } catch (TransportException e) {
            throw new EndpointAdaptorProcessingException(e);
        }

    }

    @Override
    public void removeConnectionInfo(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {
        ConcurrentHashMap<EndpointAdaptorConfiguration, LoadBalancingDataPublisher> tenantSpecificAdaptorMap = dataPublisherMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            LoadBalancingDataPublisher loadBalancingDataPublisher = tenantSpecificAdaptorMap.remove(endpointAdaptorConfiguration);
            if (loadBalancingDataPublisher != null) {
                loadBalancingDataPublisher.stop();
            }
        }
    }


}
