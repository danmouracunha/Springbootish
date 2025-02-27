package org.example.services;

import org.example.annotations.Autowired;
import org.example.annotations.Component;
import org.example.annotations.PostConstruct;
import org.example.annotations.PreDestroy;

@Component
public class DummyService {
    private final EvenDumierService evenDumierService;

    public DummyService(EvenDumierService evenDumierService) {this.evenDumierService = evenDumierService;}


    @PostConstruct
    public void init() {
        System.out.println(this.getClass().getSimpleName() + " initialized!");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println(this.getClass().getSimpleName() + " is being destroyed!");
    }

    public void doSomething() {
        System.out.println("Dont get me wrong. Im just doing something in a dummy service");
        evenDumierService.actionPoint();
    }
}
