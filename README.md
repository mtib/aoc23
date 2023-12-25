# Advent of Code 2023

in Kotlin!

## Dependencies

- [Koin](https://insert-koin.io/) (for day resolution)
- [JGraphT](https://jgrapht.org/) (for day 25)
- [OkHTTP3](https://square.github.io/okhttp/) (for downloading the puzzle input)
- [kotlinx-coroutines](https://kotlinlang.org/docs/coroutines-guide.html) (for running stuff in parallel)
- [kotlinx-serialization](https://kotlinlang.org/docs/serialization.html) (for (de-)serializing persistent data)

## How to run

1. Place puzzle input in a file named `day<num>.txt` in `src/main/resources`.
2. Run the main function in `dev.mtib.aoc23.AoC.kt` with the day number as argument.

or build the jar and run it:

```bash
gradle aocJar
java -jar app/build/libs/aoc.jar <day>
```

You can also move `.env.example` to `.env` and fill in your session cookie to automatically download the puzzle input.
You may also be required to have `openscad` in the path.

### Example Output

```
Running day 4

Part 1:
22488
Runtime: 1.6ms, σ: 2.3ms (297 runs)

Part 2:
7013204
Runtime: 1.0ms, σ: 0.6ms (500 runs)
```

(but more colorful)

## Code

Days are implemented here: [dev.mtib.aoc23.day](https://github.com/mtib/aoc_23_kt/tree/main/app/src/main/kotlin/dev/mtib/aoc23/day).
