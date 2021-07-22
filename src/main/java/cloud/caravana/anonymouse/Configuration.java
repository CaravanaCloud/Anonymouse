package cloud.caravana.anonymouse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import cloud.caravana.anonymouse.iter.JDBCIterator;
import cloud.caravana.anonymouse.report.Report;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hashids.Hashids;
import org.yaml.snakeyaml.Yaml;

@ApplicationScoped
@Default
public class Configuration {
    static final String userDir = System.getProperty("user.dir");

    @Inject
    Logger log;

    @ConfigProperty(name = "anon.prefix", defaultValue= "|#|")
    String anonPrefix;

    @ConfigProperty(name = "anon.exitOnStop", defaultValue= "true")
    boolean exitOnStop;

    @ConfigProperty(name = "anon.dryRun", defaultValue= "true")
    boolean dryRun;

    @ConfigProperty(name = "anon.waitBeforeRun", defaultValue= "10")
    long waitBeforeRun;

    @ConfigProperty(name = "jdbc.maxRows", defaultValue = "")
    Optional<Integer> maxRowsPerTable;

    @ConfigProperty(name = "jdbc.timeout_minutes", defaultValue= "5")
    Integer timeoutMinutes;

    @ConfigProperty(name = "jdbc.log_each", defaultValue= "100000")
    Integer logEach;

    @ConfigProperty(name = "hashids.salt", defaultValue= "Anonymouse")
    String hashSalt;


    Map<String, PIIClass> piiClasses = new HashMap<>();

    public Configuration(){}

    void onStart(@Observes StartupEvent ev) {
        var profile = getActiveProfile();
        var userDir = getUserDir();
        var msg = new StringBuffer();
        msg.append("Anonymouse ...\n");
        msg.append("... PID [%d]\n".formatted(getPID()));
        msg.append("... profile [%s]\n".formatted(profile));
        msg.append("... userDir [%s]\n".formatted(userDir));
        msg.append("... dryRun [%s]\n".formatted(isDryRun()));
        msg.append("... jdbc max rows per table [%d]\n".formatted(getMaxRows()));
        loadConfigFromFiles();
        msg.append("... pii classes [%d]\n".formatted(piiClasses.size()));
        log.info(msg.toString());
    }

    public Integer getMaxRows() {
        return maxRowsPerTable.orElse(null);
    }

    private long getPID() {
        return ProcessHandle.current().pid();
    }

    private void loadConfigFromFiles() {
        Path anonPath = getAnymousePath();
        loadConfig(anonPath);
    }

    public Path getAnymousePath() {
        Path userPath = Path.of(userDir);
        return userPath.resolve("anonymouse");
    }

    public Path getReportsPath(){
        return getAnymousePath().resolve("report");
    }

    private void loadConfig(Path anonPath) {
        File anonFile = anonPath.toFile();
        if (! anonFile.exists()) return;
        if (anonFile.isDirectory()){
            log.info("Loading config from directory [%s]".formatted(anonFile));
            try{
                Files.walkFileTree(anonPath, new ConfigurationFileVisitor(this));
            }catch (IOException e){
                log.info("Failed to load [%s]".formatted(anonFile));
                log.throwing("Configuraiton","loadConfig", e);
            }
        }
    }

    private String getActiveProfile() {
        return ProfileManager.getActiveProfile();
    }

    @SuppressWarnings("all")
    private static Optional<String> optEnv(String varName) {
        Map<String, String> env = System.getenv();
        return Optional.ofNullable(env.get(varName));
    }

    public final String getAnonPrefix() {
        return anonPrefix;
    }

    @Produces
    public Hashids produceHashid(){
        return new Hashids(hashSalt);
    }

    public String getUserDir(){
        return userDir;
    }

    @PostConstruct
    public void loadFromEnv(){
        optConfigFilePath().ifPresent(this::addFromURL);
    }

    public Integer getTimeoutMinutes(){
        return timeoutMinutes;
    }

    public Integer getLogEach() {
        return logEach;
    }

    Optional<String> optConfigFilePath() {
        return optEnv("ANONYM_CONFIG");
    }

    @Produces
    public final ExecutorService getExecutorService(){
        return Executors.newWorkStealingPool();
    }

    public final void add(final String url) {
        if (url == null) {
            log.warning("Cannot load null configuration URL");
            return;
        }
        addFromURL(url);
    }

    @SuppressWarnings("all")
    public void addFromURL(final String url) {
        log.info("Loading configuration from URL [%s]".formatted(url));
        String[] split = url.split(":");
        var protocol = split[0];
        switch (protocol){
            case "classpath" -> addFromResource(url);
            default -> addFromStream(url);
        }
    }

    public void addFromURL(URL url){
        try (InputStream is = url.openStream()){
            addFromInput(is);
        }catch (IOException ex){
            log.warning("Failed to read from url "+url);
        }
    }

    public void addFromStream(String url) {
        try {
            addFromURL(new URL(url));
        }catch (MalformedURLException ex){
            log.warning("Failed to parse url "+url);
        }
    }

    private void addFromResource(String url) {
        String[] split = url.split(":");
        var resource = split[1];
        try (var input = getClass().getResourceAsStream(resource)
        ) {
            addFromInput(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void addFromInput(InputStream istream) {
        if (istream != null) {
            var yaml = new Yaml();
            @SuppressWarnings("unchecked")
            var cfgMap = (Map<String, Object>) yaml.load(istream);
            if (cfgMap != null)
                cfgMap.forEach(this::addRoot);
            else
                log.warning("Null map loaded from yaml");
        } else {
            log.warning("Failed to load config");
        }
    }

    private void addRoot(final String key, final Object value) {
        if (value instanceof String) {
            PIIClass piiClass = PIIClass.valueOf((String) value);
            setPIIClass(piiClass, key);
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            map.forEach((ckey, cvalue) ->
                addChild(key, ckey, cvalue.toString()));
        }
    }

    private void addChild(final String key,
                          final String ckey,
                          final  String cvalue) {
        PIIClass piiClass = PIIClass.of(cvalue);
        String cname = cname(key,ckey);
        setPIIClass(piiClass, cname);
    }

    private void setPIIClass(final PIIClass piiClass,
                             final String cname) {
        piiClasses.put(cname.toUpperCase(), piiClass);
    }

    public final PIIClass getPIIClass(String... context) {
        var cname = cname(context);
        PIIClass piiClass = piiClasses.get(cname.toUpperCase());
        return piiClass;
    }

    private String cname(String... context) {
        return String.join(".",context);
    }

    public boolean isDeclared(String cname, PIIClass piiClass) {
        var declaredClass = getPIIClass(cname);
        boolean isDeclared = declaredClass != null && declaredClass.equals(piiClass);
        return isDeclared;
    }

    public void onJDBCReady(JDBCIterator jdbcIter, Report report) {
        if(isJDBCReady()){
            jdbcIter.setReport(report);
            jdbcIter.run();
        }
    }

    public boolean isJDBCReady() {
        String anonJDBC = "" + System.getenv().get("ANONYM_JDBC");
        if ("false".equals(anonJDBC))
            return false;
        return true;
    }

    public void onDDBReady(Runnable run) {
        String anonDDB = "" + System.getenv().get("ANONYM_DDB");
        boolean isDDBReady = "true".equals(anonDDB);
        if(isDDBReady){
            run.run();
        }
    }

    public Boolean isDryRun() {
        return dryRun;
    }

    public boolean isExitOnStop() {
        return exitOnStop;
    }

    public void beforeRunWait() {
        try {
            log.info("Waiting [%d]s".formatted(waitBeforeRun));
            Thread.sleep(waitBeforeRun * 1000L);
        } catch (InterruptedException e) {
            log.throwing("Configuration", "beforeRunWait", e);
        }
    }

    public void addFromPath(Path file) {
        try {
            addFromURL(file.toUri().toURL());
        } catch (MalformedURLException e) {
            log.warning(e.getMessage());
            log.throwing("Configuration","addFromPath",e);
        }
    }

    public File getReportOutDir() {
        return getReportsPath().toFile();
    }
}
