package org.neo4j.procedures;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
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
public class ProceduresBenchmarkState {

    private GraphDatabaseService db;
    private File neo4jDirectory;

    @Setup
    public void startNeo4j() {
        try {
            neo4jDirectory = Files.createTempDirectory("neo4j").toFile();
            db = new GraphDatabaseFactory().newEmbeddedDatabase(neo4jDirectory);
            Procedures procedures = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(Procedures.class);
            procedures.registerProcedure(TrivialProcedures.class);
            procedures.registerFunction(TrivialProcedures.class);
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

    @Benchmark
    public void testTrivialCypherStatement() {
        long result = Iterators.single(db.execute("RETURN 5 AS result").columnAs("result"));
    }

    @Benchmark
    public void testTrivialProcedureCall() {
        long result = Iterators.single(db.execute("CALL org.neo4j.procedures.trivial() YIELD value RETURN value").columnAs("value"));
    }

    @Benchmark
    public void testTrivialFunction() {
        long result = Iterators.single(db.execute("RETURN org.neo4j.procedures.trivialFunction() as value").columnAs("value"));
    }

}
