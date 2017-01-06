package org.neo4j.ogm.domain.stage.nodes;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Drama extends BaseNode {

    public Drama() {
        super();
    }

    public Drama(String title) {
        super(title);
    }
}
