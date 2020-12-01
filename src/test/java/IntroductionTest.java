import java.util.List;
import java.util.Objects;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.guava.GuavaPlugin;
import org.jdbi.v3.sqlobject.*;
import org.junit.jupiter.api.Test;

import javax.xml.transform.Result;

import static org.assertj.core.api.Assertions.assertThat;

public class IntroductionTest {
    @Test
    public void sqlObject() {
        // tag::sqlobject-usage[]
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test"); //dataSource
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new H2DatabasePlugin());
        jdbi.installPlugin(new GuavaPlugin());

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
        // end::sqlobject-usage[]
    }
}
