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
package org.wso2.carbon.event.notifier.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.internal.CarbonEventNotifierService;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;
import org.wso2.carbon.event.stream.manager.core.EventStreamListener;

public class EventStreamListenerImpl implements EventStreamListener {

    private static final Log log = LogFactory.getLog(EventStreamListenerImpl.class);

    @Override
    public void removedEventStream(int tenantId, String streamName, String streamVersion) {

        CarbonEventNotifierService carbonEventNotifierService = EventNotifierServiceValueHolder.getCarbonEventNotifierService();
        String streamNameWithVersion = streamName + ":" + streamVersion;
        try {
            carbonEventNotifierService.deactivateActiveEventFormatterConfigurationForStream(tenantId, streamNameWithVersion);
        } catch (EventNotifierConfigurationException e) {
            log.error("Exception occurred while un-deploying the Event notifier configuration files");
        }

    }

    @Override
    public void addedEventStream(int tenantId, String streamName, String streamVersion) {

        CarbonEventNotifierService carbonEventNotifierService = EventNotifierServiceValueHolder.getCarbonEventNotifierService();
        String streamNameWithVersion = streamName + ":" + streamVersion;
        try {
            carbonEventNotifierService.activateInactiveEventFormatterConfigurationForStream(tenantId, streamNameWithVersion);
        } catch (EventNotifierConfigurationException e) {
            log.error("Exception occurred while un-deploying the Event notifier configuration files");
        }

    }
}
