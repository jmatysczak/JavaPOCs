package jmat.javapocs.guicebindingoverriding;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.util.Modules;
import org.aopalliance.intercept.MethodInvocation;

public class Main {

    public static void main(final String[] args) {
        performOverriding();
        performIntercepting();
    }

    private static void performOverriding() {
        final Injector injector = Guice.createInjector(Modules.override(new Module1()).with(new Module2()));
        final Service service = injector.getInstance(Service.class);
        System.out.println("Overridden value: " + service.getValue());
    }

    private static void performIntercepting() {
        final Injector injector = Guice.createInjector(new Module1(), new Module3());
        final Service service = injector.getInstance(Service.class);
        System.out.println("Intercepted value: " + service.getValue());
    }
}

interface Service {

    String getValue();
}

class Service1 implements Service {

    @Override
    public String getValue() {
        return "Value from service 1";
    }
}

class Service2 implements Service {

    @Override
    public String getValue() {
        return "Value from service 2";
    }
}

class Module1 extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(Service.class).to(Service1.class);
    }
}

class Module2 extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(Service.class).to(Service2.class);
    }
}

class Module3 extends AbstractModule {

    @Override
    protected void configure() {
        this.bindInterceptor(
            Matchers.subclassesOf(Service.class),
            Matchers.any(),
            (final MethodInvocation methodInvocation) -> {
                final Object normalReturnValue = methodInvocation.proceed();
                System.out.println("Intercepted method: " + methodInvocation.getMethod());
                System.out.println("Normal return value: " + normalReturnValue);
                return "Intercepted value!";
            }
        );
    }
}
