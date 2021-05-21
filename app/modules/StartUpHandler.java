package modules;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import play.Application;

@Singleton
public class StartUpHandler {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Inject
    public StartUpHandler(Application app) {
        logger.info("Application has started");
    }
}
