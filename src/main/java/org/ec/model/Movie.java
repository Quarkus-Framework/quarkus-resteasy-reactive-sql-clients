package org.ec.model;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Movie {

    private Long id;
    private String title;

    public static Multi<Movie> findAll(PgPool client) {
        return client.query("SELECT id, title FROM movies ORDER BY title DESC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Movie::from);

    }

    public static Uni<Movie> findById(PgPool client, Long id) {
        return client
                .preparedQuery("SELECT id, title FROM movies WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem()
                .transform(m -> m.iterator().hasNext() ? from(m.iterator().next()) : null);
    }

    public static Uni<Long> save(PgPool client, String title) {
        return client
                .preparedQuery("INSERT INTO movies (title) VALUES ($1) RETURNING id")
                .execute(Tuple.of(title))
                .onItem()
                .transform(m -> m.iterator().next().getLong("id"));
    }

    public static Uni<Boolean> delete(PgPool client, Long id) {
        return client
                .preparedQuery("DELETE FROM movies WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem()
                .transform(m -> m.rowCount() == 1);
    }

    private static Movie from(Row row) {
        return new Movie(row.getLong("id"), row.getString("title"));
    }
}
