/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.session.remoting;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.RenewDatumRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.remoting.handler.AbstractClientHandler;

/**
 * The type Data node exchanger.
 * @author shangyu.wh
 * @version $Id : DataNodeExchanger.java, v 0.1 2017-12-01 11:51 shangyu.wh Exp $
 */
public class DataNodeExchanger implements NodeExchanger {

    private static final Logger               LOGGER          = LoggerFactory
                                                                  .getLogger(DataNodeExchanger.class);
    private static final Logger               EXCHANGE_LOGGER = LoggerFactory
                                                                  .getLogger("SESSION-EXCHANGE");

    @Autowired
    private Exchange                          boltExchange;

    @Autowired
    private SessionServerConfig               sessionServerConfig;

    @Resource(name = "dataClientHandlers")
    private Collection<AbstractClientHandler> dataClientHandlers;

    @Autowired
    private NodeManager                       dataNodeManager;

    /**
     * @see DataNodeExchanger#request(Request)
     */
    @Override
    public Response request(Request request) throws RequestException {

        Response response;
        URL url = request.getRequestUrl();
        try {
            Client sessionClient = boltExchange.getClient(Exchange.DATA_SERVER_TYPE);

            if (sessionClient == null) {
                LOGGER.warn(
                        "DataNode Exchanger get dataServer connection {} error! Connection can not be null or disconnected!",
                        url);
                //first start session maybe case sessionClient null,try to auto connect
                sessionClient = boltExchange.connect(Exchange.DATA_SERVER_TYPE, url,
                        dataClientHandlers.toArray(new ChannelHandler[dataClientHandlers.size()]));
            }
            //try to connect data
            Channel channel = sessionClient.getChannel(url);
            if (channel == null) {
                channel = sessionClient.connect(url);
            }

            // print but ignore if from renew module, cause renew request is too much
            if (!(request.getRequestBody() instanceof RenewDatumRequest)) {
                EXCHANGE_LOGGER.info("DataNode Exchanger request={},url={}", request.getRequestBody(), url);
            }

            final Object result = sessionClient
                    .sendSync(channel, request.getRequestBody(), sessionServerConfig.getDataNodeExchangeTimeOut());
            if (result == null) {
                throw new RequestException("DataNode Exchanger request data get null result!", request);
            }
            response = () -> result;
        } catch (Exception e) {
            LOGGER.error(String.format("Error when request DataNode! Request url=%s, request=%s, msg=%s", url,
                    request.getRequestBody(), e.getMessage()));
            throw new RequestException("DataNode Exchanger request data error! Request url:" + url, request, e);
        }

        return response;
    }

    @Override
    public Client connectServer() {

        Collection<Node> dataNodes = dataNodeManager.getDataCenterNodes();
        if (dataNodes == null || dataNodes.isEmpty()) {
            dataNodeManager.getAllDataCenterNodes();
            dataNodes = dataNodeManager.getDataCenterNodes();
        }

        Client dataClient = null;
        for (Node dataNode : dataNodes) {
            if (dataNode.getNodeUrl() == null || dataNode.getNodeUrl().getIpAddress() == null) {
                LOGGER.error("get data node address error!url{}", dataNode.getNodeUrl());
                continue;
            }

            URL url = new URL(dataNode.getNodeUrl().getIpAddress(),
                sessionServerConfig.getDataServerPort());
            try {
                dataClient = boltExchange.getClient(Exchange.DATA_SERVER_TYPE);
                if (dataClient == null) {
                    dataClient = boltExchange.connect(Exchange.DATA_SERVER_TYPE, url,
                        dataClientHandlers.toArray(new ChannelHandler[dataClientHandlers.size()]));
                }
            } catch (Exception e) {
                LOGGER.error("DataNode Exchanger connect DataServer error!url:" + url, e);
                continue;
            }

            try {
                Channel channel = dataClient.getChannel(url);
                if (channel == null) {
                    dataClient.connect(url);
                }
            } catch (Exception e) {
                LOGGER.error("DataNode Exchanger connect channel error!url:" + url, e);
            }
        }
        return dataClient;
    }
}