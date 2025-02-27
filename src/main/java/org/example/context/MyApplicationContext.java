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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
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
            Object bean = createBeanWithConstructor(clazz);
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
                    String simpleName = field.getType().getSimpleName();
                    String beanName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                    Object dependency = beans.get(beanName);
                    if (dependency == null) {
                        throw new RuntimeException("Dependency not found: " + beanName);
                    }
                    field.setAccessible(true);
                    field.set(bean, dependency);
                }
            }
        }
    }

    private Object createBeanWithConstructor(Class<?> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1) {
            Constructor<?> constructor = constructors[0];
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = getBeanByType(paramTypes[i]);
            }

            constructor.setAccessible(true);
            return constructor.newInstance(params);
        } else {
            throw new RuntimeException("No unique constructor found for class: " + clazz.getName());
        }
    }

    private Object getBeanByType(Class<?> type) {
        List<Object> candidates = beans.values().stream()
                .filter(bean -> type.isAssignableFrom(bean.getClass()))
                .toList();

        if (candidates.isEmpty()) {
            throw new RuntimeException("No bean found for type: " + type.getName());
        } else if (candidates.size() > 1) {
            throw new RuntimeException("Multiple beans found for type: " + type.getName());
        }

        return candidates.get(0);
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