/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

/**
 * <p>Apache Dubbo service provider filter that enables integration with Sentinel. Auto activated by default.</p>
 * <p>Note: this only works for Apache Dubbo 2.7.x or above version.</p>
 * <p>
 * If you want to disable the provider filter, you can configure:
 * <pre>
 * &lt;dubbo:provider filter="-sentinel.dubbo.provider.filter"/&gt;
 * </pre>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
@Activate(group = "provider")
public class SentinelDubboProviderFilter extends BaseSentinelDubboFilter {

    public SentinelDubboProviderFilter() {
        RecordLog.info("Sentinel Apache Dubbo provider filter initialized");
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // Get origin caller.
        String application = DubboUtils.getApplication(invocation, "");
        RpcContext rpcContext = RpcContext.getContext();
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        try {
            String resourceName = DubboUtils.getResourceName(invoker, invocation);
            String interfaceName = invoker.getUrl().getEncodedServiceKey();
            // Only need to create entrance context at provider side, as context will take effect
            // at entrance of invocation chain only (for inbound traffic).
            ContextUtil.enter(resourceName, application);
            interfaceEntry = SphU.entry(interfaceName, EntryType.IN);
            methodEntry = SphU.entry(resourceName, EntryType.IN, 1, invocation.getArguments());
            rpcContext.set(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY, interfaceEntry);
            rpcContext.set(DubboUtils.DUBBO_METHOD_ENTRY_KEY, methodEntry);
            return invoker.invoke(invocation);
        } catch (BlockException e) {
            return DubboFallbackRegistry.getProviderFallback().handle(invoker, invocation, e);
        } catch (RpcException e) {
            trace(e, invocation);
            throw e;
        }
    }


}

