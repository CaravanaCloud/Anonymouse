package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Anonymouse;
import cloud.caravana.anonymouse.idj.entity.Person;
import cloud.caravana.anonymouse.idj.repo.PersonRepo;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CPFClassifierTest {
    @Inject
    Anonymouse anonymouse;

    @Inject
    PersonRepo personRepo;



    @Test
    public void testCPFClassifier(){
        //given
        createPerson();
        assertTrue(personRepo.existsPIICPF());
        personRepo.printAll();

        anonymouse.declare("anonym_person.cpf","CPF");

        //when
        runAnonymouse();

        personRepo.printAll();
        //then
        assertFalse(personRepo.existsPIICPF());
        System.out.println("Done");
    }

    private void runAnonymouse() {
        anonymouse.doRun();
    }


    void createPerson() {
        Person person = Person.of(UUID.randomUUID().hashCode(),"Fulano de Tal", "44722832714");
        personRepo.insert(person);
    }
}