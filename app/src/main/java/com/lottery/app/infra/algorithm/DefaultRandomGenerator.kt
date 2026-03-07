package com.lottery.app.infra.algorithm

import com.lottery.app.domain.generator.LotteryGenerator
import com.lottery.app.domain.model.*
import java.security.SecureRandom

class DefaultRandomGenerator : LotteryGenerator {

    private val random = SecureRandom()

    override fun generateStandard(
        lotteryType: LotteryType,
        count: Int,
        strategy: GeneratorStrategy
    ): List<LotteryNumber> {
        val rule = LotteryRule.of(lotteryType)
        return (1..count).map {
            LotteryNumber(
                frontNumbers = pickByStrategy(rule.frontMin, rule.frontMax, rule.frontPickCount, strategy),
                backNumbers = pickByStrategy(rule.backMin, rule.backMax, rule.backPickCount, strategy)
            )
        }
    }

    override fun generateMultiple(lotteryType: LotteryType, config: MultipleConfig): LotteryNumber {
        val rule = LotteryRule.of(lotteryType)
        return LotteryNumber(
            frontNumbers = pickRandom(rule.frontMin, rule.frontMax, config.frontCount),
            backNumbers = pickRandom(rule.backMin, rule.backMax, config.backCount)
        )
    }

    override fun generateDanTuo(lotteryType: LotteryType, config: DanTuoConfig): DanTuoNumber {
        val rule = LotteryRule.of(lotteryType)

        val frontDan = pickRandom(rule.frontMin, rule.frontMax, config.frontDanCount)
        val frontTuo = pickRandomExcluding(
            rule.frontMin, rule.frontMax, config.frontTuoCount, frontDan.toSet()
        )

        val backDan: List<Int>
        val backTuo: List<Int>
        if (config.backDanCount > 0) {
            backDan = pickRandom(rule.backMin, rule.backMax, config.backDanCount)
            backTuo = pickRandomExcluding(
                rule.backMin, rule.backMax, config.backTuoCount, backDan.toSet()
            )
        } else {
            backDan = emptyList()
            backTuo = pickRandom(rule.backMin, rule.backMax, config.backTuoCount)
        }

        return DanTuoNumber(
            frontDan = frontDan,
            frontTuo = frontTuo,
            backDan = backDan,
            backTuo = backTuo
        )
    }

    private fun pickByStrategy(min: Int, max: Int, count: Int, strategy: GeneratorStrategy): List<Int> {
        return when (strategy) {
            GeneratorStrategy.PURE_RANDOM -> pickRandom(min, max, count)
            GeneratorStrategy.TAIL_PRIORITY -> pickTailPriority(min, max, count)
            GeneratorStrategy.HOT_COLD_BALANCE -> pickHotColdBalance(min, max, count)
            GeneratorStrategy.ODD_EVEN_BALANCE -> pickOddEvenBalance(min, max, count)
            GeneratorStrategy.BIG_SMALL_BALANCE -> pickBigSmallBalance(min, max, count)
        }
    }

    /**
     * Tail Priority: pick a random tail digit (0-9), then prefer numbers ending with that digit.
     * Fill remaining slots with random picks from the rest.
     */
    private fun pickTailPriority(min: Int, max: Int, count: Int): List<Int> {
        val targetTail = random.nextInt(10)
        val pool = (min..max).toList()
        val withTail = pool.filter { it % 10 == targetTail }.toMutableList()
        val withoutTail = pool.filter { it % 10 != targetTail }.toMutableList()

        val result = mutableListOf<Int>()
        val tailCount = minOf(withTail.size, (count + 1) / 2)
        repeat(tailCount) {
            result.add(withTail.removeAt(random.nextInt(withTail.size)))
        }
        val remaining = count - result.size
        val others = withoutTail.toMutableList()
        repeat(remaining) {
            result.add(others.removeAt(random.nextInt(others.size)))
        }
        return result.sorted()
    }

    /**
     * Hot/Cold Balance: divide the pool into equal segments and sample uniformly from each.
     */
    private fun pickHotColdBalance(min: Int, max: Int, count: Int): List<Int> {
        val pool = (min..max).toList()
        val segmentSize = pool.size / count
        val result = mutableListOf<Int>()
        val used = mutableSetOf<Int>()

        for (i in 0 until count) {
            val segStart = i * segmentSize
            val segEnd = if (i == count - 1) pool.size else (i + 1) * segmentSize
            val segment = pool.subList(segStart, segEnd).filter { it !in used }
            if (segment.isNotEmpty()) {
                val pick = segment[random.nextInt(segment.size)]
                result.add(pick)
                used.add(pick)
            }
        }

        if (result.size < count) {
            val remaining = pool.filter { it !in used }.toMutableList()
            while (result.size < count && remaining.isNotEmpty()) {
                result.add(remaining.removeAt(random.nextInt(remaining.size)))
            }
        }
        return result.sorted()
    }

    /**
     * Odd/Even Balance: force roughly half odd, half even.
     */
    private fun pickOddEvenBalance(min: Int, max: Int, count: Int): List<Int> {
        val pool = (min..max).toList()
        val odds = pool.filter { it % 2 == 1 }.toMutableList()
        val evens = pool.filter { it % 2 == 0 }.toMutableList()

        val oddCount = (count + 1) / 2
        val evenCount = count - oddCount

        val result = mutableListOf<Int>()
        val actualOddCount = minOf(oddCount, odds.size)
        repeat(actualOddCount) {
            result.add(odds.removeAt(random.nextInt(odds.size)))
        }
        val actualEvenCount = minOf(evenCount, evens.size)
        repeat(actualEvenCount) {
            result.add(evens.removeAt(random.nextInt(evens.size)))
        }

        val remaining = count - result.size
        if (remaining > 0) {
            val leftover = (odds + evens).toMutableList()
            repeat(remaining) {
                result.add(leftover.removeAt(random.nextInt(leftover.size)))
            }
        }
        return result.sorted()
    }

    /**
     * Big/Small Balance: split by midpoint, pick half from each side.
     */
    private fun pickBigSmallBalance(min: Int, max: Int, count: Int): List<Int> {
        val mid = (min + max) / 2
        val smalls = (min..mid).toMutableList()
        val bigs = ((mid + 1)..max).toMutableList()

        val smallCount = (count + 1) / 2
        val bigCount = count - smallCount

        val result = mutableListOf<Int>()
        val actualSmallCount = minOf(smallCount, smalls.size)
        repeat(actualSmallCount) {
            result.add(smalls.removeAt(random.nextInt(smalls.size)))
        }
        val actualBigCount = minOf(bigCount, bigs.size)
        repeat(actualBigCount) {
            result.add(bigs.removeAt(random.nextInt(bigs.size)))
        }

        val remaining = count - result.size
        if (remaining > 0) {
            val leftover = (smalls + bigs).toMutableList()
            repeat(remaining) {
                result.add(leftover.removeAt(random.nextInt(leftover.size)))
            }
        }
        return result.sorted()
    }

    private fun pickRandom(min: Int, max: Int, count: Int): List<Int> {
        val pool = (min..max).toMutableList()
        val result = mutableListOf<Int>()
        repeat(count) {
            val index = random.nextInt(pool.size)
            result.add(pool.removeAt(index))
        }
        return result.sorted()
    }

    private fun pickRandomExcluding(
        min: Int, max: Int, count: Int, exclude: Set<Int>
    ): List<Int> {
        val pool = (min..max).filter { it !in exclude }.toMutableList()
        val result = mutableListOf<Int>()
        repeat(count) {
            val index = random.nextInt(pool.size)
            result.add(pool.removeAt(index))
        }
        return result.sorted()
    }
}
