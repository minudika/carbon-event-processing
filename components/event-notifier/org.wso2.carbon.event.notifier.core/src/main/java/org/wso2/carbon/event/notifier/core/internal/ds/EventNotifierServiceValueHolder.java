/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.notifier.core.internal.ds;

import org.wso2.carbon.event.notifier.core.EventNotifierService;
import org.wso2.carbon.event.notifier.core.MessageType;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorService;
import org.wso2.carbon.event.notifier.core.config.OutputMapperFactory;
import org.wso2.carbon.event.notifier.core.internal.CarbonEventNotifierService;
import org.wso2.carbon.event.notifier.core.internal.CarbonOutputEventAdaptorService;
import org.wso2.carbon.event.notifier.core.internal.type.json.JSONOutputMapperFactory;
import org.wso2.carbon.event.notifier.core.internal.type.map.MapOutputMapperFactory;
import org.wso2.carbon.event.notifier.core.internal.type.text.TextOutputMapperFactory;
import org.wso2.carbon.event.notifier.core.internal.type.wso2event.WSO2OutputMapperFactory;
import org.wso2.carbon.event.notifier.core.internal.type.xml.XMLOutputMapperFactory;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.concurrent.ConcurrentHashMap;

public class EventNotifierServiceValueHolder {

    private static CarbonEventNotifierService carbonEventNotifierService;
    private static CarbonOutputEventAdaptorService carbonOutputEventAdaptorService;
    private static EventStreamService eventStreamService;
    private static RegistryService registryService;
    private static ConcurrentHashMap<String, OutputMapperFactory> mappingFactoryMap = new ConcurrentHashMap<String, OutputMapperFactory>() {
    };

    static {
        mappingFactoryMap.put(MessageType.MAP, new MapOutputMapperFactory());
        mappingFactoryMap.put(MessageType.TEXT, new TextOutputMapperFactory());
        mappingFactoryMap.put(MessageType.WSO2EVENT, new WSO2OutputMapperFactory());
        mappingFactoryMap.put(MessageType.XML, new XMLOutputMapperFactory());
        mappingFactoryMap.put(MessageType.JSON, new JSONOutputMapperFactory());
    }

    private static EventStatisticsService eventStatisticsService;

    private EventNotifierServiceValueHolder() {

    }

    public static CarbonEventNotifierService getCarbonEventNotifierService() {
        return carbonEventNotifierService;
    }

    public static void registerEventNotifierService(EventNotifierService eventNotifierService) {
        EventNotifierServiceValueHolder.carbonEventNotifierService = (CarbonEventNotifierService) eventNotifierService;

    }

    public static void setRegistryService(RegistryService registryService) {
        EventNotifierServiceValueHolder.registryService = registryService;
    }

    public static void unSetRegistryService() {
        EventNotifierServiceValueHolder.registryService = null;
    }

    public static RegistryService getRegistryService() {
        return EventNotifierServiceValueHolder.registryService;
    }

    public static Registry getRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static ConcurrentHashMap<String, OutputMapperFactory> getMappingFactoryMap() {
        return mappingFactoryMap;
    }

    public static void registerEventStatisticsService(
            EventStatisticsService eventStatisticsService) {
        EventNotifierServiceValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }

    public static void registerEventStreamService(EventStreamService eventStreamService) {
        EventNotifierServiceValueHolder.eventStreamService = eventStreamService;
    }

    public static EventStreamService getEventStreamService() {
        return EventNotifierServiceValueHolder.eventStreamService;
    }

    public static CarbonOutputEventAdaptorService getOutputEventAdaptorService() {
        return carbonOutputEventAdaptorService;
    }

    public static void registerOutputEventAdaptorService(CarbonOutputEventAdaptorService carbonOutputEventAdaptorService) {
        EventNotifierServiceValueHolder.carbonOutputEventAdaptorService = carbonOutputEventAdaptorService;
    }
}
