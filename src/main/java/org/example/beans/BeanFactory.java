package org.example.beans;

import java.util.HashMap;
import java.util.Map;

public class BeanFactory {
    private Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    private Map<String, Object> singletonBeans = new HashMap<>();

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitions.put(beanName, beanDefinition);
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitions.get(beanName);
        if (beanDefinition == null) {
            throw new RuntimeException("Bean not found: " + beanName);
        }

        if ("singleton".equals(beanDefinition.getScope())) {
            Object bean = singletonBeans.get(beanName);
            if (bean == null) {
                bean = createBean(beanDefinition);
                singletonBeans.put(beanName, bean);
            }
            return bean;
        } else {
            return createBean(beanDefinition);
        }
    }

    private Object createBean(BeanDefinition beanDefinition) {
        try {
            Object bean = beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
            new DependencyInjector(this).injectDependencies(bean);
            return bean;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean", e);
        }
    }
}
