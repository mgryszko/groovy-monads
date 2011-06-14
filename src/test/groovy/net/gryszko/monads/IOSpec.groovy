package net.gryszko.monads

import spock.lang.Specification

abstract class IOAction {
    static IOAction unit(value) {
        new UnitAction(value: value)
    }

    static IOAction create(Closure io) {
        new SimpleAction(call: io)
    }

    IOAction bind(Closure f) {
        new ChainedAction(prevAction: this, nextActionFn: f)
    }

    private static class UnitAction extends IOAction {
        def value
        def call = { value }
    }

    private static class SimpleAction extends IOAction {
        def call = { throw new NoSuchMethodException("No IO expression defined yet") }
    }

    private static class ChainedAction extends IOAction {
        IOAction prevAction
        Closure nextActionFn

        def call = {
            def intermediateResult = (this.prevAction)()
            def nextAction = nextActionFn(intermediateResult)
            nextAction()
        }
    }
}

class IOActions {
    static openFile(name) {
        IOAction.create({ -> new File(name)})
    }
    static readFile(file) {
        IOAction.create({ -> file.readLines().join(';')})
    }
}

class IOSpec extends Specification
{
    def "IO monad reads a file"()
    {
        setup:
        def openAction = IOActions.openFile("sample_file.txt")

        expect:
        openAction.bind({ file -> IOActions.readFile(file) })() == "first line;second line"
    }

    def "unit method takes a value and transforms it into a monad"() {
        expect:
        IOAction.unit(1) instanceof IOAction.UnitAction
    }

    def "first monadic law: identity - m.bind { x -> unit(x) } ≡ m"() {
        setup:
        def monad = IOActions.openFile("sample_file.txt")
        def f = { file -> IOAction.unit(file) }

        when:
        def nextMonad = monad.bind(f)

        then:
        monad() == nextMonad()
    }

    def "second monadic law: unit - unit(x).flatMap(f) ≡ f(x)"() {
        setup:
        def f = { name -> IOActions.openFile(name) }


        when:
        def nextMonad = IOAction.unit("sample_file.txt").bind(f)

        then:
        nextMonad() == f("sample_file.txt")()
    }

    def "third monadic law: composition - m.flatMap(f).flatMap(g) ≡ m.flatMap{ x -> f(x).flatMap(g) }"() {
        setup:
        def monad = IOActions.openFile("sample_file.txt")
        def f = { file -> IOActions.readFile(file) }
        def g = { contents -> IOAction.create { [contents, contents].join("-") } }

        when:
        def nextMonadLeft = monad.bind(f).bind(g)
        def nextMonadRight = monad.bind({ file -> f(file).bind(g) })

        then:
        nextMonadLeft() == nextMonadRight()
    }
}
