package nz.ac.wgtn.nullinference.experiments.spring;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

/**
 * Log system to be used.
 * @author jens dietrich
 */
public class LogSystem {

    static {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);
    }

    public static Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }
}
