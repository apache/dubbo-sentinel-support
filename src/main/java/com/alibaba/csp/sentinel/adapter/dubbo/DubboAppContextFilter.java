package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * Puts current consumer's application name in the attachment of each invocation.
 *
 * @author Eric Zhao
 */
@Activate(group = "consumer")
public class DubboAppContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String application = invoker.getUrl().getParameter(Constants.APPLICATION_KEY);
        if (application != null) {
            RpcContext.getContext().setAttachment(DubboUtils.DUBBO_APPLICATION_KEY, application);
        }
        return invoker.invoke(invocation);
    }
}
