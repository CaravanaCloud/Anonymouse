package cloud.caravana.anonymouse;

public abstract class Configuration {

    abstract PIIClass getPIIClass(String cname);

    abstract void add(String url);

    public boolean isDeclared(String cname,
                                    PIIClass piiClass) {
        return getPIIClass(cname).equals(piiClass);
    }
}
