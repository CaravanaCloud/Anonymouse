package cloud.caravana.anonymouse;

import cloud.caravana.anonymouse.util.Utils;

import javax.inject.Inject;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

import static cloud.caravana.anonymouse.util.Utils.isConfig;
import static java.nio.file.FileVisitResult.CONTINUE;

public class ConfigurationFileVisitor extends SimpleFileVisitor<Path> {
    Logger logger = Logger.getLogger("ConfigurationFileVisitor");
    Configuration cfg;


    public ConfigurationFileVisitor(Configuration cfg) {
        this.cfg = cfg;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr){
        logger.info("Loading configuraton file "+file);
        if(isConfig(file))
            cfg.addFromPath(file);
        return CONTINUE;
    }
}
