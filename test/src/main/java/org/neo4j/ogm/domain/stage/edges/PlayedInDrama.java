package org.neo4j.ogm.domain.stage.edges;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.domain.stage.nodes.Drama;
import org.neo4j.ogm.domain.stage.nodes.StageActor;

@RelationshipEntity(type = "PLAYED_IN")
public class PlayedInDrama extends BaseEdge<StageActor, Drama> {

    public PlayedInDrama() {
        super();
    }

    public PlayedInDrama(StageActor start, Drama end, String title) {
        super(start, end, title);
    }
}
