package com.alibaba.csp.sentinel.adapter.dubbo;


import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;

public abstract class BaseSentinelDubboFilter implements Filter {

    @Override
    public Result onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        //it's unnecessary to trace the business exception
        trace(null, invocation);
        return appResponse;
    }


    static void trace(Throwable throwable, Invocation invocation) {
        Entry interfaceEntry = (Entry) RpcContext.getContext().get(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY);
        Entry methodEntry = (Entry) RpcContext.getContext().get(DubboUtils.DUBBO_METHOD_ENTRY_KEY);
        if (methodEntry != null) {
            Tracer.traceEntry(throwable, methodEntry);
            methodEntry.exit();
            RpcContext.getContext().remove(DubboUtils.DUBBO_METHOD_ENTRY_KEY);
        }
        if (interfaceEntry != null) {
            Tracer.traceEntry(throwable, interfaceEntry);
            interfaceEntry.exit();
            RpcContext.getContext().remove(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY);
        }
        if (!(interfaceEntry instanceof AsyncEntry)) {
            ContextUtil.exit();
        }
    }
}
