package dev.mtib.aoc23.day

import dev.mtib.aoc23.utils.AbstractDay
import org.jgrapht.alg.StoerWagnerMinimumCut
import org.jgrapht.graph.SimpleGraph
import org.koin.core.annotation.Single

@Single
class Day25 : AbstractDay(25) {

    data class Node(val name: String)
    data class Edge private constructor(val from: Node, val to: Node) {
        companion object {
            operator fun invoke(from: Node, to: Node): Edge {
                return if (from.name < to.name) {
                    Edge(from, to)
                } else {
                    Edge(to, from)
                }
            }
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        val nodes = mutableSetOf<Node>()
        val edges = mutableSetOf<Edge>()
        for (line in input) {
            val connectedNodeNames = line.split(":", " ").filter { it.isNotBlank() }
            val connectedNodes = connectedNodeNames.map { Node(it) }
            nodes.addAll(connectedNodes)
            edges.addAll(connectedNodes.subList(1, connectedNodes.size).map { node ->
                Edge(node, connectedNodes[0])
            })
        }
        debug {
            println("Nodes: ${nodes.size}")
            println("Edges: ${edges.size}")
        }

        val graph = SimpleGraph<Node, Edge>(Edge::class.java)
        nodes.forEach { graph.addVertex(it) }
        edges.forEach { graph.addEdge(it.from, it.to, it) }

        val cut = StoerWagnerMinimumCut(graph)
        val compA = cut.minCut().filterNotNull().toSet()
        val compB = nodes - compA
        debug {
            println("Cut: ${cut.minCutWeight()}")
            println("CompA: ${compA.size}")
            println("CompB: ${compB.size}")
        }
        return compA.size * compB.size
    }

    override fun solvePart2(input: Array<String>): Any? {
        return 49
    }
}