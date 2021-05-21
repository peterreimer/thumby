package modules;

import com.google.inject.AbstractModule;

public class OnStartupModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(StartUpHandler.class).asEagerSingleton();
    }

}
