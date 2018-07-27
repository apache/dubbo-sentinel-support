package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 运行该用例的时候需要加入JVM参数：-Djava.net.preferIPv4Stack=true，
 * 否则可能会抛出{@code java.lang.IllegalStateException: Can't assign requested address}
 *
 * @author leyou
 */
public class DubboProviderTest {

    private final String resource = "com.alibaba.csp.sentinel.adapter.dubbo.DemoService:sayHello(java.lang.String,int)";

    @Test
    public void testProviderFilter() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
            new String[] {"spring-dubbo-provider-filter.xml"});
        context.start();
        DemoService demoService = (DemoService)context.getBean("demoService");
        String result = demoService.sayHello("Test dubbo provider filter", 1);
        System.out.println("result=" + result);
        ClusterNode node = ClusterBuilderSlot.getClusterNode(resource);
        Thread.sleep(1000 * 60 * 100);
        Assert.assertNotNull(node);
    }
}
