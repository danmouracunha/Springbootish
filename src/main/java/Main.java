import org.example.context.MyApplicationContext;
import org.example.services.DummyService;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        System.out.println("Im just a dummy springboot(ish) application");
        MyApplicationContext context = new MyApplicationContext();

        context.scanAndInitializeBeans("org.org.example.services");

        DummyService myService = (DummyService) context.getBean("dummyService");
        myService.doSomething();

        context.destroyBeans();
    }
}