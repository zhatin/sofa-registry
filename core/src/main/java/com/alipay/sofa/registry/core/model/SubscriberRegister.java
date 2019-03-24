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
package com.alipay.sofa.registry.core.model;

/**
 *
 * @author zhuoyu.sjw
 * @version $Id: SubscriberRegister.java, v 0.1 2017-11-28 15:40 zhuoyu.sjw Exp $$
 */
public class SubscriberRegister extends BaseRegister {
    private static final long serialVersionUID = 5884257055097046886L;

    private String            scope;

    /**
     * Getter method for property <tt>scope</tt>.
     *
     * @return property value of scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Setter method for property <tt>scope</tt>.
     *
     * @param scope value to be assigned to property scope
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "SubscriberRegister{" + "scope='" + scope + '\'' + '}' + super.toString();
    }
}
