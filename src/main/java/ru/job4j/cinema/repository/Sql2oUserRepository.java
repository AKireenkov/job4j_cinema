package ru.job4j.cinema.repository;

import org.springframework.stereotype.Repository;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.job4j.cinema.model.User;

import java.util.Collection;
import java.util.Optional;

@Repository
public class Sql2oUserRepository implements UserRepository {
    private final Sql2o sql2o;

    public Sql2oUserRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Optional<User> save(User user) {
        try (var connection = sql2o.open()) {
            var sql = """
                    INSERT INTO users (full_name, email, password)
                    VALUES (:fullName, :email, :password)
                    """;
            var query = connection.createQuery(sql, true)
                    .addParameter("fullName", user.getFullName())
                    .addParameter("email", user.getEmail())
                    .addParameter("password", user.getPassword());

            Connection generatedId = null;
            try {
                generatedId = query.executeUpdate();
                user.setId(generatedId.getKey(Integer.class));
            } catch (IllegalArgumentException iae) {
                return Optional.empty();
            }
            return Optional.of(user);
        }
    }

    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        try (var connection = sql2o.open()) {
            var sql = """
                    SELECT * FROM users
                    WHERE email=:email AND password=:password
                    """;
            var query = connection.createQuery(sql)
                    .addParameter("email", email)
                    .addParameter("password", password);
            var user = query.setColumnMappings(User.COLUMN_MAPPING).executeAndFetchFirst(User.class);
            return Optional.ofNullable(user);
        }
    }

    @Override
    public Collection<User> findAll() {
        try (var connection = sql2o.open()) {
            var sql = """
                    SELECT * FROM users
                    """;
            var query = connection.createQuery(sql);
            return query.setColumnMappings(User.COLUMN_MAPPING).executeAndFetch(User.class);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (var connection = sql2o.open()) {
            var sql = """
                    DELETE * FROM users
                    WHERE id=:id
                    """;
            var query = connection.createQuery(sql)
                    .addParameter("id", id);
            return query.executeUpdate().getResult() > 0;
        }
    }
}
