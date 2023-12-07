package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.koin.core.annotation.Single

@Single
class Day7 : AbstractDay(7) {
    enum class Card(val label: Char, val value: Int) {
        TWO('2', 1),
        THREE('3', 2),
        FOUR('4', 3),
        FIVE('5', 4),
        SIX('6', 5),
        SEVEN('7', 6),
        EIGHT('8', 7),
        NINE('9', 8),
        TEN('T', 9),
        JACK('J', 10),
        QUEEN('Q', 11),
        KING('K', 12),
        ACE('A', 13);

        fun getValue(withJoker: Boolean): Int {
            if (!withJoker) {
                return value
            }
            when (this) {
                JACK -> return 0
                else -> return value
            }
        }
    }

    data class Hand(val cards: List<Card>, val winnings: Int) {
        companion object {
            fun from(input: String): Hand {
                val (cardString, winningString) = input.split(" ")
                val cards = cardString.asIterable().map { char -> Card.entries.find { it.label == char }!! }
                require(cards.size == 5)
                return Hand(cards, winningString.toInt())
            }
        }

        fun cardsToString() = cards.joinToString("") { it.label.toString() }

        fun isFiveOfKind(withJoker: Boolean = false): Boolean {
            if (withJoker) {
                return cards.groupBy { it.value }.values.any { it.size == 5 - if (it[0] != Card.JACK) cards.count { it == Card.JACK } else 0 }
            }
            return cards.groupBy { it.value }.values.any { it.size == 5 }
        }

        fun isFourOfKind(withJoker: Boolean = false): Boolean {
            if (withJoker) {
                return cards.groupBy { it.value }.values.any { it.size == 4 - if (it[0] != Card.JACK) cards.count { it == Card.JACK } else 0 }
            }
            return cards.groupBy { it.value }.values.any { it.size == 4 }
        }

        fun isFullHouse(withJoker: Boolean = false): Boolean {
            if (withJoker) {
                var badLastCardType: Card? = null
                return (cards.groupBy { it.value }.values.any { it.size == 3 } &&
                        cards.groupBy { it.value }.values.any { it.size == 2 }) ||
                        (cards.groupBy { it.value }.values.any {
                            if (it.size == 3 - if (it[0] != Card.JACK) cards.count { it == Card.JACK } else 0) {
                                badLastCardType = it[0]
                                true
                            } else {
                                false
                            }
                        } &&
                                cards.groupBy { it.value }.values.any { it.size == 2 && it[0] != Card.JACK && it[0] != badLastCardType }) ||
                        (cards.groupBy { it.value }.values.any { it.size == 3 && it[0] != Card.JACK } &&
                                cards.groupBy { it.value }.values.any { it.size == 2 - if (it[0] != Card.JACK) cards.count { it == Card.JACK } else 0 })
            }
            return cards.groupBy { it.value }.values.any { it.size == 3 } &&
                    cards.groupBy { it.value }.values.any { it.size == 2 }
        }

        fun isThreeOfKind(withJoker: Boolean = false): Boolean {
            if (withJoker) {
                return cards.groupBy { it.value }.values.any { it.size == 3 - if (it[0] != Card.JACK) cards.count { it == Card.JACK } else 0 }
            }
            return cards.groupBy { it.value }.values.any { it.size == 3 }
        }

        fun isTwoPair(withJoker: Boolean = false): Boolean {
            // Jokers wouldn't make this, they'd turn it into 3 of a kind instead
            return cards.groupBy { it.value }.values.filter { it.size == 2 }.size == 2
        }

        fun isOnePair(withJoker: Boolean = false): Boolean {
            if (withJoker) {
                return cards.groupBy { it.value }.values.any { it.size == 2 - if (it[0] != Card.JACK) cards.count { it == Card.JACK } else 0 }
            }
            return cards.groupBy { it.value }.values.any { it.size == 2 }
        }

        enum class HandType {
            FIVE_OF_KIND,
            FOUR_OF_KIND,
            FULL_HOUSE,
            THREE_OF_KIND,
            TWO_PAIR,
            ONE_PAIR,
            HIGH_CARD
        }

        fun getHandType(withJoker: Boolean = false): HandType {
            return when {
                isFiveOfKind(withJoker) -> HandType.FIVE_OF_KIND
                isFourOfKind(withJoker) -> HandType.FOUR_OF_KIND
                isFullHouse(withJoker) -> HandType.FULL_HOUSE
                isThreeOfKind(withJoker) -> HandType.THREE_OF_KIND
                isTwoPair(withJoker) -> HandType.TWO_PAIR
                isOnePair(withJoker) -> HandType.ONE_PAIR
                else -> HandType.HIGH_CARD
            }
        }

        fun compare(other: Hand, withJoker: Boolean = false): Int {
            val thisType = getHandType(withJoker)
            val otherType = other.getHandType(withJoker)

            if (thisType != otherType) {
                return -thisType.compareTo(otherType)
            }

            for ((thisCard, otherCard) in cards.zip(other.cards)) {
                if (thisCard.value == otherCard.value) {
                    continue
                }
                return thisCard.getValue(withJoker).compareTo(otherCard.getValue(withJoker))
            }
            throw Exception("Hands are equal")
        }
    }

    override fun solvePart1(input: Array<String>): String? {
        val hands = input.filter { it.isNotBlank() }.map { Hand.from(it) }

        require(Hand.from("33332 1").compare(Hand.from("2AAAA 1")) == 1) { "33332 1 > 2AAAA 1" }
        require(Hand.from("77888 1").compare(Hand.from("77788 1")) == 1) { "77888 1 > 77788 1" }

        return hands.sortedWith { o1, o2 -> o1.compare(o2) }.withIndex()
            .sumOf { (index, hand) -> hand.winnings.toLong() * (index.toLong() + 1) }.toString()
    }

    override fun solvePart2(input: Array<String>): String? {
        val hands = input.filter { it.isNotBlank() }.map { Hand.from(it) }

        require(Hand.from("3444J 1").compare(Hand.from("3344J 1")) == 1) { "3444J 1 > 3344J 1" }
        require(
            Hand.from("T55J5 1").getHandType(true) == Hand.HandType.FOUR_OF_KIND
        ) { "T55J5 1 is four of a kind" }
        require(
            Hand.from("KTJJT 1").getHandType(true) == Hand.HandType.FOUR_OF_KIND
        ) { "KTJJT 1 is four of a kind" }
        require(
            Hand.from("QQQJA 1").getHandType(true) == Hand.HandType.FOUR_OF_KIND
        ) { "QQQJA 1 is four of a kind" }
        require(
            Hand.from("T55J5 1").compare(Hand.from("QQQJA 1"), true) == -1
        ) { "T55J5 1 < QQQJA 1" }
        require(
            Hand.from("QQQJA 1").compare(Hand.from("KTJJT 1"), true) == -1
        ) { "QQQJA 1 < KTJJT 1" }
        require(Hand.from("J2233 1").getHandType(true) == Hand.HandType.FULL_HOUSE) { "J2233 is full house" }
        require(
            Hand.from("JJ234 1").getHandType(true) == Hand.HandType.THREE_OF_KIND
        ) { "JJ234 is three of a kind (${Hand.from("JJ234 1").getHandType(true)})" }
        require(
            Hand.from("JJ233 1").getHandType(true) == Hand.HandType.FOUR_OF_KIND
        ) { "JJ233 is four of a kind" }
        require(
            Hand.from("JJJ23 1").getHandType(true) == Hand.HandType.FOUR_OF_KIND
        ) { "JJJ23 is four of a kind" }
        require(
            Hand.from("JJJA2 1").compare(Hand.from("JJAA2 1"), true) == -1
        ) { "JJJA2 1 > JJAA2 1" }
        require(
            Hand.from("AAKJT 1").getHandType(true) == Hand.HandType.THREE_OF_KIND
        ) { "AAKJT is three of a kind (${Hand.from("AAKJT 1").getHandType(true)})" }

        return hands.sortedWith { o1, o2 -> o1.compare(o2, true) }.also {
            log(it.joinToString("\n| ") { "${it.cardsToString()} ${it.getHandType(true)}" })
        }.withIndex()
            .sumOf { (index, hand) -> hand.winnings.toLong() * (index.toLong() + 1) }.toString()
    }
}