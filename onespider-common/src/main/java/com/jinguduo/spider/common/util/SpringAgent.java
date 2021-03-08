package com.jinguduo.spider.common.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringAgent implements ApplicationContextAware, BeanFactoryPostProcessor {

	private static ApplicationContext appContext;
	private static ConfigurableListableBeanFactory beanFactory;
	
	public static void autowireBean(Object existingBean) {
		beanFactory.autowireBean(existingBean);
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
		beanFactory = factory;
	}

	public void setApplicationContext(ApplicationContext c) throws BeansException {
		appContext = c;
	}

	public static ApplicationContext getAppContext() {
		return appContext;
	}

	public ConfigurableListableBeanFactory getFactory() {
		return beanFactory;
	}
}
