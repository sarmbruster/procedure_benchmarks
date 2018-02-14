package org.neo4j.procedures;

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
}
