package org.neo4j.ogm.domain.stage.edges;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.domain.stage.nodes.FilmActor;
import org.neo4j.ogm.domain.stage.nodes.Movie;

@RelationshipEntity(type = "LAST_APPEARANCE")
public class LastMovie extends BaseEdge<FilmActor, Movie> {

    public LastMovie() {
        super();
    }

    public LastMovie(FilmActor start, Movie end, String title) {
        super(start, end, title);
    }
}
