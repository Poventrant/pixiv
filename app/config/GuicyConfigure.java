package config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import config.guice.CommonGuiceModule;
import play.modules.guice.GuiceSupport;

public class GuicyConfigure extends GuiceSupport {
    protected Injector configure() {
        Injector injector = Guice.createInjector(new CommonGuiceModule());
        return injector;
    }
}