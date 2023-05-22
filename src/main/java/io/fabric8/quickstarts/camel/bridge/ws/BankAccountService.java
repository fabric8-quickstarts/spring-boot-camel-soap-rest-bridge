/*
 *  Copyright 2005-2023 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.quickstarts.camel.bridge.ws;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace = "io.fabric8.quickstarts")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface BankAccountService {

    @WebMethod(operationName = "getBalance")
    @WebResult(name = "balance")
    int getBalance();

    @WebMethod(operationName = "deposit")
    void deposit(int amount);

}