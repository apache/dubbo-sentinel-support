package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * <p>Dubbo service provider filter for Sentinel. Auto activated by default.</p>
 *
 * If you want to disable the provider filter, you can configure:
 * <pre>
 * &lt;dubbo:provider filter="-sentinel.dubbo.provider.filter"/&gt;
 * </pre>
 *
 * @author leyou
 */
@Activate(group = "provider")
public class SentinelDubboProviderFilter extends AbstractDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // Get origin caller.
        String application = DubboUtils.getApplication(invocation, "");

        Entry interfaceEntry = null;
        Entry methodEntry = null;
        try {
            String resourceName = getResourceName(invoker, invocation);
            String interfaceName = invoker.getInterface().getName();
            ContextUtil.enter(resourceName, application);
            interfaceEntry = SphU.entry(interfaceName, EntryType.IN);
            methodEntry = SphU.entry(resourceName, EntryType.IN, 1, invocation.getArguments());

            return invoker.invoke(invocation);
        } catch (BlockException e) {
            throw new SentinelRpcException(e);
        } catch (RpcException e) {
            Tracer.trace(e);
            throw e;
        } finally {
            if (methodEntry != null) {
                methodEntry.exit(1, invocation.getArguments());
            }
            if (interfaceEntry != null) {
                interfaceEntry.exit();
            }
            ContextUtil.exit();
        }
    }
}
