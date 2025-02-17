package org.example.services;

import org.example.annotations.Component;
import org.example.annotations.PostConstruct;
import org.example.annotations.PreDestroy;

@Component
public class DummyService {
    @PostConstruct
    public void init() {
        System.out.println("bean initialized!");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("bean is being destroyed!");
    }

    public void doSomething() {
        System.out.println("Dont get me wrong. Im just doing something in a dummy service");
    }
}
