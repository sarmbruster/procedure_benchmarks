package org.neo4j.procedures;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@State(Scope.Benchmark)
public class MultiFunctionBenchmarkState {

    private GraphDatabaseService db;
    private File neo4jDirectory;
    private Label label = Label.label("MyLabel");

    @Setup
    public void startNeo4j() {
        try {
            neo4jDirectory = Files.createTempDirectory("neo4j").toFile();
            db = new GraphDatabaseFactory().newEmbeddedDatabase(neo4jDirectory);
            Procedures procedures = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(Procedures.class);
            procedures.registerProcedure(TrivialProcedures.class);
            procedures.registerFunction(TrivialProcedures.class);
            try (Transaction tx = db.beginTx()) {
                for (int i=0; i<20_000; i++) {
                    Node node = db.createNode(label);
                    node.setProperty("value", 5l);
                }
                tx.success();
            }
        } catch (IOException|KernelException e) {
            throw new RuntimeException(e);
        }
    }

    @TearDown
    public void stopNeo4j() {
        db.shutdown();
        try {
            FileUtils.deleteDirectory(neo4jDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*@Benchmark
    public void testTrivialCypherStatement() {
        long result = Iterators.single(db.execute("MATCH (n:MyLabel) WHERE n.value=5 RETURN count(*) as result").columnAs("result"));
    }*/

/*
    @Benchmark
    public void testTrivialProcedureCall() {
        long result = Iterators.single(db.execute("CALL org.neo4j.procedures.trivial() YIELD value RETURN value").columnAs("value"));
    }
*/

    /*@Benchmark
    public void testTrivialFunction() {
        long result = Iterators.single(db.execute("MATCH (n:MyLabel) WHERE n.value=org.neo4j.procedures.trivialFunction() RETURN count(*) as result").columnAs("result"));
    }*/

    @Benchmark
    public void testMultipleInvocationFunction() {
        long result = Iterators.single(db.execute("UNWIND range(0,10000) AS x RETURN sum(org.neo4j.procedures.trivialFunction()) as result").columnAs("result"));
    }

    @Benchmark
    public void baseline() {
        double result = Iterators.single(db.execute("UNWIND range(0,10000) AS x RETURN sum(round(5.1)) as result").columnAs("result"));
    }
}
