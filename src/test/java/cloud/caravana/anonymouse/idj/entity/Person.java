package cloud.caravana.anonymouse.idj.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table(name = "anonym_person")
public class Person extends PanacheEntity{
    @Column(name = "fullName")
    String fullName;

    @Column(name = "nickName")
    String nickName;

    @Column(name = "cpf")
    String cpf;

    @Column(name = "birthDay")
    LocalDate birthDay;

    @Column(name = "primaryEmail")
    String primaryEmail;

    @Column(name = "primaryPhone")
    String primaryPhone;

    public static Person of(long id, String fullName, String cpf) {
        var person = new Person();
        person.id = id;
        person.fullName = fullName;
        person.cpf = cpf;
        return person;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNickName() {
        return nickName;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    @Override
    public String toString() {
        return "Person{" +
                " id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", nickName='" + nickName + '\'' +
                ", cpf='" + cpf + '\'' +
                ", birthDay=" + birthDay +
                ", primaryEmail='" + primaryEmail + '\'' +
                ", primaryPhone='" + primaryPhone + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }
}
