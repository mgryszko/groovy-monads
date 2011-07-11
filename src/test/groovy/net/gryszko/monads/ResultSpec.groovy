package net.gryszko.monads

import spock.lang.Specification
import spock.lang.FailsWith

class __ {
}

abstract class Result<T> {
    static Result<T> unit(T value) {
        // TBD
    }

    abstract Result<T> bind(Closure f)

    abstract Result<T> orElse(Result<T> o)
}

class Success<T> extends Result<T> {
    private T value

    Success(T value) {
        // TBD
    }

    Result<T> bind(Closure f) { // T -> Result<T>
        // TBD
    }

    Result<T> orElse(Result<T> o) {
        // TBD
    }

    def methodMissing(String name, args) {
        MetaMethod method = value.metaClass.methods.find { it.name == name }
        if (method) {
            method.invoke(value, args)
        }
        else throw new MissingMethodException(name, value, args)
    }

    boolean equals(o) {
        if (!(o instanceof Success)) return false

        value == o.value
    }

    int hashCode() {
        value != null ? value.hashCode() : 0
    }
}

class Failure<T> extends Result<T> {

    Result<T> bind(Closure f) { // T -> Result<T>
        // TBD
    }

    Result<T> orElse(Result<T> o) {
        // TBD
    }

    def methodMissing(String name, args) {
        throw new RuntimeException("Operation failure")
    }

    boolean equals(o) {
        o instanceof Failure
    }

    int hashCode() {
        0
    }
}

class ResultSpec extends Specification {

    def "unit method takes a value and transforms it into a monad"() {
        expect:
        Result.unit(1) instanceof __
    }

    def "unit in Java/Groovy is same as constructor"() {
        expect:
        Result.unit(1) == __
    }

    def "in case of success all method calls are delegated to the underlying value"() {
        setup:
        def result = new Success("hello!")

        expect:
        result.length() == __
    }

    @FailsWith(__)
    def "in case of failure all method calls raise an exception"() {
        setup:
        def result = new Failure()

        expect:
        result.length()
    }

    def "first monadic law: identity - m.bind { x -> unit(x) } ≡ m"() {
        setup:
        def mSome = new Success(1)
        def mNone = new Failure()
        def f = __

        expect:
        mSome.bind(f) == __
        mNone.bind(f) == __
    }

    def "second monadic law: unit - unit(x).flatMap(f) ≡ f(x)"() {
        setup:
        def f = __

        expect:
        __ == f(1)
    }

    def "third monadic law: composition - m.flatMap(f).flatMap(g) ≡ m.flatMap{ x -> f(x).flatMap(g) }"() {
        setup:
        def m = Result.unit(1)
        def f = __
        def g = __

        expect:
        m.bind(f).bind(g) == __
    }

    def "bonus: first monadic zero law: identity - mzero.flatMap(f) ≡ mzero"() {
        setup:
        def mzero = __
        def f = { x -> Success(x) }

        expect:
        mzero.bind(f) == mzero
    }

    def "bonus: second monadic zero law: binding with monadic zero - m.flatMap({ x -> mzero }) ≡ mzero"() {
        setup:
        def m = new Success(1)
        def mzero = __

        expect:
        __ == mzero
    }

    def "bonus: third and fourth monadic zero law: plus - mzero.plus(m) ≡ m, m.plus(mzero) ≡ m"() {
        setup:
        def m = new Success(1)
        def mzero = __

        expect:
        m.orElse(mzero) == __
        mzero.orElse(m) == __
    }
}
