package org.neo4j.procedures;

import org.junit.*;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TimeoutTest {

    public static final int TIMEOUT_SECONDS = 5;
    private GraphDatabaseService db;

    @Rule
    public Stopwatch stopwatch = new Stopwatch() {
        @Override
        protected void succeeded(long nanos, Description description) {
            System.out.println("succeeded " + nanos/1_000_000  + " " + description);
        }

        @Override
        protected void failed(long nanos, Throwable e, Description description) {
            System.out.println("failed " + nanos/1_000_000 + " " + description);
        }

        @Override
        protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
        }

        @Override
        protected void finished(long nanos, Description description) {
        }
    };

    @Before
    public void setup() throws KernelException {
        final GraphDatabaseBuilder builder = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder();
        builder.setConfig(GraphDatabaseSettings.transaction_timeout, TIMEOUT_SECONDS + "s");
        db = builder.newGraphDatabase();
        final Procedures procedures = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(Procedures.class);
        procedures.registerProcedure(TrivialProcedures.class);
        db.execute("create (p:Person{name:'John'})");
    }

    @After
    public void teatDown() {
        db.shutdown();
    }

//    @Test(expected = GuardTimeoutException.class)
    @Test(expected = TransactionTerminatedException.class)
    public void shouldTerminateLongRunningQuery() {

        long count = Iterators.single(db.execute(
                "UNWIND range(0,1000000) AS x " +
                        "MATCH (p:Person{name:'John'}) " +
                        "CALL org.neo4j.procedures.sleep(500) YIELD value " +
                        "RETURN count(*) as count").columnAs("count"));
        System.out.println("got " + count + " results");
    }

    @Test
    public void shouldNotTerminateLongRunningQuery() {

        try {
            long value = Iterators.single(db.execute(
                    "CALL org.neo4j.procedures.sleep(" + (TIMEOUT_SECONDS * 2) * 1000 + ") YIELD value " +
                            "RETURN value").columnAs("value"));

            fail("should not be successful");
        } catch (TransactionFailureException e) {
            // pass, expected
        }
        // we took longer than timeout
        assertTrue(stopwatch.runtime(TimeUnit.SECONDS)>(TIMEOUT_SECONDS+1));
    }

}
