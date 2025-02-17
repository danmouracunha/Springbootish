package org.example.beans;

public class BeanDefinition {
    private Class<?> beanClass;
    private String scope = "singleton";
    private boolean lazyInit = false;// Getters and setters

    public String getScope() {
        return scope;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }
}
