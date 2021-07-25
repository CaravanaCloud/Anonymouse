package cloud.caravana.anonymouse.idj.repo;

import cloud.caravana.anonymouse.classifier.CPFClassifier;
import cloud.caravana.anonymouse.idj.entity.Person;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.stream.Stream;

@ApplicationScoped
public class PersonRepo {

    @Inject
    DataSource dataSource;

    public boolean existsPIICPF() {
        var sql = "select cpf from anonym_person";
        try(var conn = dataSource.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql)){
            while (rs.next()){
                var cpf = rs.getString("cpf");
                if (CPFClassifier.isPIICPF(cpf))
                    return true;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public void printAll() {
        var sql = "select * from anonym_person";
        try(var conn = dataSource.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(sql)){
            while (rs.next()){
                var cpf = rs.getString("cpf");
                var fullName = rs.getString("fullName");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }


    public void insert(Person person) {
        var sql = "insert into anonym_person(id, fullName, cpf) values (?, ?, ?)";
        try( var conn = dataSource.getConnection();
            var stmt = conn.prepareStatement(sql)){
            conn.setAutoCommit(true);
            stmt.setLong (1, person.getId());
            stmt.setString(2, person.getFullName());
            stmt.setString(3, person.getCpf());
            stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
