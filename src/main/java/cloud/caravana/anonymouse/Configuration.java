package cloud.caravana.anonymouse;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface Configuration {

    PIIClass getPIIClass(String cname);

    void add(String url);
}
