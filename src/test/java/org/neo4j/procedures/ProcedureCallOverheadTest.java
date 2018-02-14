package org.neo4j.procedures;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.helpers.collection.Iterators;

import static org.junit.Assert.assertEquals;

public class ProcedureCallOverheadTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule().withProcedure(TrivialProcedures.class);

    @Test
    public void testTrivialCypherStatement() {
        long result = Iterators.single(neo4j.getGraphDatabaseService().execute("RETURN 5 AS result").columnAs("result"));
        assertEquals(5L, result);
    }

    @Test
    public void testTrivialProcedureCall() {
        long result = Iterators.single(neo4j.getGraphDatabaseService().execute("CALL org.neo4j.procedures.trivial() YIELD value RETURN value").columnAs("value"));
        assertEquals(5L, result);
    }
}