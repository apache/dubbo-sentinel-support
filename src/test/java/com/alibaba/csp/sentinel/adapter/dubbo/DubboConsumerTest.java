package com.alibaba.csp.sentinel.adapter.dubbo;

import java.util.Arrays;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 运行该用例的时候需要加入JVM参数：-Djava.net.preferIPv4Stack=true，
 * 否则可能会抛出{@code java.lang.IllegalStateException: Can't assign requested address}
 *
 * @author leyou
 */
public class DubboConsumerTest {
    private final String resource = "com.alibaba.csp.sentinel.adapter.dubbo.DemoService:sayHello(java.lang.String,int)";
    private final String interfaceResource = "com.alibaba.csp.sentinel.adapter.dubbo.DemoService";
    private ClassPathXmlApplicationContext context;

    @Before
    public void init() {
        context = new ClassPathXmlApplicationContext(
            new String[] {"spring-dubbo-consumer-filter.xml"});
        context.start();
    }

    @Test
    public void testConsumerFilter() throws Exception {
        DemoService demoService = (DemoService)context.getBean("demoService");
        String result = demoService.sayHello("Test dubbo consumer filter", 2);
        System.out.println("result=" + result);
        ClusterNode node = ClusterBuilderSlot.getClusterNode(resource);
        Assert.assertNotNull(node);
    }

    @Test(expected = SentinelRpcException.class)
    public void testConsumerBlock() throws Exception {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource(resource);
        flowRule.setCount(10);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setLimitApp("default");
        FlowRuleManager.loadRules(Arrays.asList(flowRule));

        DemoService demoService = (DemoService)context.getBean("demoService");
        for (int i = 0; i < 100; i++) {
            demoService.sayHello("Test dubbo consumer filter", 2);
        }
    }

    @Test(expected = SentinelRpcException.class)
    public void testConsumerBlock2() throws Exception {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource(interfaceResource);
        flowRule.setCount(10);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setLimitApp("default");
        FlowRuleManager.loadRules(Arrays.asList(flowRule));

        DemoService demoService = (DemoService)context.getBean("demoService");
        for (int i = 0; i < 100; i++) {
            demoService.sayHello("Test dubbo consumer filter", 2);
        }
    }

}
