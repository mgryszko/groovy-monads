package net.gryszko.monads

import spock.lang.Specification

class State<S, V>  {

    // call: S -> [S, V]
    // State function takes a state and outputs a new state with a return value
    def call = { throw new NoSuchMethodException("No state function defined yet") }

    // unit produces the given value without changing the state
    static State<S, V> unit(V value) {
        state { s -> [s, value] }
    }

    static <S, V> State<S, V> state(Closure sf) {
        sf.resolveStrategy = Closure.DELEGATE_FIRST
        sf.delegate = new State<S, V>(call: sf)
    }

    S nextState(S s) { this.call(s)[0] }
    V value(S s) { this.call(s)[1] }

    // f: V -> State(S -> [S, V])
    State bind(Closure f) {
        state { state ->
            def (nextState, value) = this.call(state)
            def stateFromF = f(value)
            stateFromF(nextState) // [S, V]
        }
    }
}

class StateSpec extends Specification {

    def "unit method takes a value and transforms it into a monad"() {
        expect:
        State.unit(1) instanceof State
    }

    def "unit doesn't change the state"() {
        setup:
        def monad = State.unit(1)

        expect:
        monad.nextState("begin") == "begin"
    }

    def "unit yields a values that doesn't depend on the state"() {
        setup:
        def monad = State.unit(1)

        expect:
        monad.value("start") == 1
        monad.value("end") == 1
    }

    def "first monadic law: identity - m.bind { x -> unit(x) } ≡ m"() {
        setup:
        def monad = State.state { s -> [s + "z", s.length()] }
        def f = { v -> State.unit(v) }

        when:
        def nextMonad = monad.bind(f)

        then:
        monad.nextState("xy") == "xyz"
        monad.value("xy") == 2
        monad.nextState("xy") == nextMonad.nextState("xy")
        monad.value("xy") == nextMonad.value("xy")
    }

    def "second monadic law: unit - unit(x).flatMap(f) ≡ f(x)"() {
        setup:
        def f = { v -> State.state { s -> [s + [1], v + s.size()] } }

        when:
        def nextMonad = State.unit(1).bind(f)

        then:
        f(1).nextState([1, 2]) == [1, 2, 1]
        f(1).value([1, 2]) == 3
        nextMonad.nextState([1, 2]) == f(1).nextState([1, 2])
        nextMonad.value([1, 2]) == f(1).value([1, 2])
    }

    def "third monadic law: composition - m.flatMap(f).flatMap(g) ≡ m.flatMap{ x -> f(x).flatMap(g) }"() {
        setup:
        def monad = State.state { s -> [s * 2, "Current result: ${s}"] }
        def f = { v -> State.state { s -> [s - 4, v + "; and then: ${s}"]} }
        def g = { v -> State.state { s -> [100, v + "; later: ${s}"] } }

        when:
        def nextMonadLeft = monad.bind(f).bind(g)
        def nextMonadRight = monad.bind({ x -> f(x).bind(g) })

        then:
        monad.nextState(2) == 4
        monad.value(2) == "Current result: 2"
        nextMonadLeft.nextState(2) == 100
        nextMonadLeft.value(2) == "Current result: 2; and then: 4; later: 0"

        nextMonadLeft.nextState(2) == nextMonadRight.nextState(2)
        nextMonadLeft.value(2) == nextMonadRight.value(2)
    }
}
