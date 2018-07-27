package com.alibaba.csp.sentinel.adapter.dubbo.provider;

import com.alibaba.csp.sentinel.adapter.dubbo.DemoService;

/**
 * @author leyou
 */
public class DemoServiceImpl implements DemoService {
    public String sayHello(String name, int n) {
        return "Hello " + name + ", " + n;
    }
}
