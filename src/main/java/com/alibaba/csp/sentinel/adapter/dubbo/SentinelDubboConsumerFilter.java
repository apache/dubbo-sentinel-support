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
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.support.RpcUtils;

/**
 * <p>Dubbo service consumer filter for Sentinel. Auto activated by default.</p>
 * <p>
 * If you want to disable the consumer filter, you can configure:
 * <pre>
 * &lt;dubbo:consumer filter="-sentinel.dubbo.consumer.filter"/&gt;
 * </pre>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
@Activate(group = "consumer")
public class SentinelDubboConsumerFilter extends BaseSentinelDubboFilter {

    public SentinelDubboConsumerFilter() {
        RecordLog.info("Sentinel Apache Dubbo consumer filter initialized");
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Entry interfaceEntry = null;
        Entry methodEntry = null;
        RpcContext rpcContext = RpcContext.getContext();
        try {
            boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
            String resourceName = DubboUtils.getResourceName(invoker, invocation);
            if (!isAsync) {
                interfaceEntry = SphU.entry(invoker.getUrl().getEncodedServiceKey(), EntryType.OUT);
                methodEntry = SphU.entry(resourceName, EntryType.OUT);
            } else {
                // should generate the AsyncEntry when the invoke model in future or async
                interfaceEntry = SphU.asyncEntry(invoker.getUrl().getEncodedServiceKey(), EntryType.OUT);
                methodEntry = SphU.asyncEntry(resourceName, EntryType.OUT);
            }
            rpcContext.set(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY, interfaceEntry);
            rpcContext.set(DubboUtils.DUBBO_METHOD_ENTRY_KEY, methodEntry);
            Result result = invoker.invoke(invocation);
            if (result instanceof AsyncRpcResult) {
                // catch timeout or nonbiz-exception when in async model
                AsyncRpcResult asyncRpcResult = (AsyncRpcResult) result;
                asyncRpcResult.getValueFuture().whenComplete((rs,ex) -> {
                    if (ex != null){
                        trace((Throwable) ex, invocation);
                    }
                });
            }
            return result;
        } catch (BlockException e) {
            return DubboFallbackRegistry.getConsumerFallback().handle(invoker, invocation, e);
        } catch (RpcException e) {
            // catch timeout or nonbiz-exception when in sync model
            trace(e, invocation);
            throw e;
        }
    }


}
