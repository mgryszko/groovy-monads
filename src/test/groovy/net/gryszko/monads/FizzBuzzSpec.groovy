package net.gryszko.monads

import spock.lang.Unroll
import spock.lang.Specification
import com.sun.org.apache.bcel.internal.generic.AALOAD

class FizzyBuzzy {
    static boolean divisibleBy(Number self, divisor) {
        self % divisor == 0
    }
}

class FizzBuzzSpec extends Specification {

    def fizzBuzz = { number ->
        def answer
        use(FizzyBuzzy) {
            if (number.divisibleBy(3) && number.divisibleBy(5)) {
                answer = "FizzBuzz"
            } else if (number.divisibleBy(3)) {
                answer = "Fizz"
            } else if (number.divisibleBy(5)) {
                answer = "Buzz"
            } else {
                answer = number.toString()
            }
        }

        [number + 1, answer]
    }

    @Unroll("says #answer for number #number")
    def "generates a complete FizzBuzz sequence"() {
        setup:
        def monad = State.state(fizzBuzz)
        1..(number - 1).each { monad.bind({ State.state(fizzBuzz) }) }

        expect:
        monad.value(number) == answer
        monad.nextState(number) == number + 1

        where:
        number | answer
        1   | "1"
        2   | "2"
        3   | "Fizz"
        4   | "4"
        5   | "Buzz"
        15  | "FizzBuzz"
        99  | "Fizz"
        100 | "Buzz"
    }
}
