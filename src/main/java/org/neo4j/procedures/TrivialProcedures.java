package org.neo4j.procedures;

import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.UserFunction;

import java.util.stream.Stream;

public class TrivialProcedures {

    @Procedure
    public Stream<LongValue> trivial() {
        return Stream.of(new LongValue(5l));
    }

    @UserFunction
    public long trivialFunction() {
        return 5l;
    }

    @Procedure
    public Stream<LongValue> sleep(@Name("duration") long millis) {
        try {
            System.out.println("waiting....");
            Thread.sleep(millis);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Stream.of(new LongValue(millis));
    }
}
