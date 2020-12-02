import java.util.List;
import java.util.Objects;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.guava.GuavaPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.Test;

import javax.xml.transform.Result;

import static org.assertj.core.api.Assertions.assertThat;

public class IntroductionTest {
    @Test
    public void sqlLoginH2Annotetion() {
        // tag::sqlobject-usage[]
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test"); //dataSource
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new H2DatabasePlugin());
        //jdbi.installPlugin(new GuavaPlugin());

        /*jdbi.useExtension(UserDao.class, userDao -> userDao.insertPositional(1, "Alice"));
        User result = jdbi.withExtension(UserDao.class, userDao -> userDao.findById(1));*/

        // Jdbi implements your interface based on annotations
        List<User> userNames = jdbi.withExtension(UserDao.class, dao -> {
            dao.createTable();
            dao.insertPositional(0, "Alice");
            dao.insertPositional(1, "Bob");
            dao.insertNamed(2, "Clarice");
            dao.insertBean(new User(3, "David"));

            return dao.listUsers();
        });

        assertThat(userNames).containsExactly(
                new User(0, "Alice"),
                new User(1, "Bob"),
                new User(2, "Clarice"),
                new User(3, "David"));
    }

    @Test
    public void sqlWithautAnnotetion() {
        //https://jdbi.org/
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test"); // (H2 in-memory database)

        List<User> users = jdbi.withHandle(handle -> {
            handle.execute("CREATE TABLE user (id INTEGER PRIMARY KEY, name VARCHAR)");

            // Inline positional parameters
            handle.execute("INSERT INTO user(id, name) VALUES (?, ?)", 0, "Alice");

            // Positional parameters
            handle.createUpdate("INSERT INTO user(id, name) VALUES (?, ?)")
                    .bind(0, 1) // 0-based parameter indexes
                    .bind(1, "Bob")
                    .execute();

            // Named parameters
            handle.createUpdate("INSERT INTO user(id, name) VALUES (:id, :name)")
                    .bind("id", 2)
                    .bind("name", "Clarice")
                    .execute();

            // Named parameters from bean properties
            handle.createUpdate("INSERT INTO user(id, name) VALUES (:id, :name)")
                    .bindBean(new User(3, "David"))
                    .execute();

            // Easy mapping to any type
            List<User> userList = handle.createQuery("SELECT * FROM user ORDER BY name")
                    .mapToBean(User.class)
                    .list();
            for (User user : userList) {
                System.out.println(user);
            }
            return userList;
        });

        assertThat(users).containsExactly(
                new User(0, "Alice"),
                new User(1, "Bob"),
                new User(2, "Clarice"),
                new User(3, "David"));
    }
}
