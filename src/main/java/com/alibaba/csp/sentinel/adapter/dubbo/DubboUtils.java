package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.dubbo.rpc.Invocation;

/**
 * @author Eric Zhao
 */
public final class DubboUtils {

    public static final String DUBBO_APPLICATION_KEY = "dubboApplication";

    public static String getApplication(Invocation invocation, String defaultValue) {
        if (invocation == null || invocation.getAttachments() == null) {
            throw new IllegalArgumentException("Bad invocation instance");
        }
        return invocation.getAttachment(DUBBO_APPLICATION_KEY, defaultValue);
    }

    private DubboUtils() {}
}
