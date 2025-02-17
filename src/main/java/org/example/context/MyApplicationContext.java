package org.example.context;

import org.example.annotations.Autowired;
import org.example.annotations.Component;
import org.example.annotations.PostConstruct;
import org.example.annotations.PreDestroy;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyApplicationContext {
    private Map<String, Object> beans = new HashMap<>();

    public void registerBean(String beanName, Object bean) {
        beans.put(beanName, bean);
    }

    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    public void scanAndInitializeBeans(String basePackage) throws Exception {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(basePackage))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));

        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);

        for (Class<?> clazz : componentClasses) {
            String beanName = getBeanName(clazz);
            Object bean = clazz.getDeclaredConstructor().newInstance();
            registerBean(beanName, bean);
        }

        injectDependencies();

        invokePostConstructMethods();
    }

    private String getBeanName(Class<?> clazz) {
        Component componentAnnotation = clazz.getAnnotation(Component.class);
        String beanName = componentAnnotation.value();
        if (beanName.isEmpty()) {
            beanName = clazz.getSimpleName();
            beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
        }
        return beanName;
    }

    private void injectDependencies() throws Exception {
        for (Object bean : beans.values()) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object dependency = beans.get(field.getType().getSimpleName());
                    if (dependency == null) {
                        throw new RuntimeException("Dependency not found: " + field.getType().getSimpleName());
                    }
                    field.setAccessible(true);
                    field.set(bean, dependency);
                }
            }
        }
    }

    private void invokePostConstructMethods() throws Exception {
        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    method.setAccessible(true);
                    method.invoke(bean);
                }
            }
        }
    }

    public void destroyBeans() throws Exception {
        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(PreDestroy.class)) {
                    method.setAccessible(true);
                    method.invoke(bean);
                }
            }
        }
    }
}