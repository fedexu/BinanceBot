package com.fedexu.binancebot.configuration.runtime;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.wss.BinanceBotMain;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;


@Service
public class BinanceBotMainRunner implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Autowired
    private GenericWebApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BinanceBotMainRunner.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void addBean(String beanId, String symbol, CandlestickInterval interval) {

        AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
        ConfigurableApplicationContext configContext = (ConfigurableApplicationContext) applicationContext;
        SingletonBeanRegistry beanRegistry = configContext.getBeanFactory();

        String beanName = symbol + "_" + interval;
        if (!registry.isBeanNameInUse(beanName)) {
            BinanceBotMain bean = (BinanceBotMain) factory.createBean(BinanceBotMain.class, AUTOWIRE_CONSTRUCTOR, false);
            bean.TIME_INTERVAL = interval;
            bean.market = symbol;
            beanRegistry.registerSingleton(beanName, bean);
        }
    }

    public void removeBean(String beanId) {
        AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
        ((DefaultListableBeanFactory) registry).destroySingleton(beanId);
    }

    public List<String> getAllBean(){
        AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
        return Arrays.asList(((DefaultListableBeanFactory) registry).getBeanNamesForType(BinanceBotMain.class));
    }

}