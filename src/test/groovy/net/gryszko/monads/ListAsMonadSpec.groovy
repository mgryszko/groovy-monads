package net.gryszko.monads

import spock.lang.Specification

class ListAsMonadSpec extends Specification {
    private <T> List<T> unit(T[] elements) {
        [*elements]
    }

    def setup() {
        List.metaClass.flatMap = { Closure f -> // T -> List<T>
            def intermediateMonads = delegate.collect { x -> f(x) }
            intermediateMonads.flatten()
        }
    }

    def "unit method takes a value and transforms it into a monad"() {
        expect:
        unit(1, 2) instanceof List
    }

    def "unit in Java/Groovy is same as constructor"() {
        expect:
        unit(1, 2) == [1, 2]
    }

    def "first monadic law: identity - m.flatMap { x -> unit(x) } ≡ m"() {
        setup:
        def m = unit(1, 2)
        def f = { x -> unit(x) }

        expect:
        m.flatMap(f) == m
    }

    def "second monadic law: unit - unit(x).flatMap(f) ≡ f(x)"() {
        setup:
        def f = { x -> [x + 1, x + 2] }

        expect:
        unit(1).flatMap(f) == f(1)
    }

    def "third monadic law: composition - m.flatMap(f).flatMap(g) ≡ m.flatMap{ x -> f(x).flatMap(g) }"() {
        setup:
        def m = unit(1, 2)
        def f = { x -> unit(2 * x) }
        def g = { x -> unit("number - ${x}", "number - ${x + 1}") }

        expect:
        m.flatMap(f).flatMap(g) == m.flatMap({ x -> f(x).flatMap(g) })
    }

    def "bonus: first monadic zero law: identity - mzero.flatMap(f) ≡ mzero"() {
        setup:
        def mzero = unit()
        def f = { x -> [x, x + 1] }

        expect:
        mzero.flatMap(f) == mzero
    }

    def "bonus: second monadic zero law: binding with monadic zero - m.flatMap({ x -> mzero }) ≡ mzero"() {
        setup:
        def m = unit(1, 2)
        def mzero = unit()

        expect:
        m.flatMap({ x -> mzero }) == mzero
    }

    def "bonus: third and fourth monadic zero law: plus - mzero.plus(m) ≡ m, m.plus(mzero) ≡ m"() {
        setup:
        def m = unit(1, 2)
        def mzero = unit()

        expect:
        m.plus(mzero) == m
        mzero.plus(m) == m
    }
}
