package org.example.beans;

import org.example.annotations.Autowired;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class DependencyInjector {
    private BeanFactory beanFactory;
    public DependencyInjector(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void injectDependencies(Object bean) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = beanFactory.getBean(field.getType().getSimpleName());
                field.setAccessible(true);
                try {
                    field.set(bean, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency", e);
                }
            }
        }
    }
}