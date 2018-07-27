package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * @author leyou
 */
abstract class AbstractDubboFilter implements Filter {

    protected String getResourceName(Invoker<?> invoker, Invocation invocation) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(invoker.getInterface().getName())
            .append(":")
            .append(invocation.getMethodName())
            .append("(");
        boolean isFirst = true;
        for (Class<?> clazz : invocation.getParameterTypes()) {
            if (!isFirst) {
                buf.append(",");
            }
            buf.append(clazz.getName());
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }
}
