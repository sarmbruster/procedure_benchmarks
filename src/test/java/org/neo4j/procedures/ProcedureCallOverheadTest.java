package org.neo4j.procedures;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.helpers.collection.Iterators;

import static org.junit.Assert.assertEquals;

public class ProcedureCallOverheadTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withProcedure(TrivialProcedures.class)
            .withFunction(TrivialProcedures.class);

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

    @Test
    public void testTrivialFunction() {
        long result = Iterators.single(neo4j.getGraphDatabaseService().execute("RETURN org.neo4j.procedures.trivialFunction() as value").columnAs("value"));
        assertEquals(5L, result);
    }


    @Test
    public void printQueryPlans() {

        Result result = neo4j.getGraphDatabaseService().execute("PROFILE MATCH (n:MyLabel) WHERE n.value=size('abcde') RETURN count(*) as result");
        Iterators.asList(result);
        System.out.println(result.getExecutionPlanDescription().toString());

        result = neo4j.getGraphDatabaseService().execute("PROFILE MATCH (n:MyLabel) WHERE n.value=org.neo4j.procedures.trivialFunction() RETURN count(*) as result");
        Iterators.asList(result);
        System.out.println(result.getExecutionPlanDescription().toString());

//        long result = Iterators.single(db..columnAs("result"));
    }

    @Test
    public void validateMultipleInvocationStatement() {

        final long result = Iterators.single(neo4j.getGraphDatabaseService().execute("UNWIND range(1,10000) AS x RETURN sum(org.neo4j.procedures.trivialFunction()) as result").columnAs("result"));
        assertEquals(10000*5l, result);
    }

}
