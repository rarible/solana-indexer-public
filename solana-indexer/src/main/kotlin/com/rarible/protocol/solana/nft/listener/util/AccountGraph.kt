package com.rarible.protocol.solana.nft.listener.util

class AccountGraph(
    /*

    Links of each account to it's group, group is completely the same ref for each account of the group
    Example:
    a -> [b, c]
    b -> [a, c]
    c -> [a, b]
    e -> [d]
    d -> [e]

    in such case expected groups, where each account of the group mapped to it's group, will be:
    a -> [a, b, c]
    b -> [a, b, c]
    c -> [a, b, c]
    d -> [d, e]
    e -> [d, e]

    */
    private val ribs: MutableMap<String, MutableSet<String>> = HashMap()
) {

    fun addRib(from: String, to: String) {
        ribs.computeIfAbsent(from) { HashSet(8) }.add(to)
        ribs.computeIfAbsent(to) { HashSet(8) }.add(from)
    }

    fun findGroups(): Map<String, AccountGroup> {
        val copy = HashMap(ribs)
        val groups = HashMap<String, AccountGroup>(ribs.size)
        while (copy.size > 0) {
            val visited = HashSet<String>()
            val head = copy.keys.iterator().next()
            visit(visited, copy, head)
            val accountGroup = AccountGroup(visited)
            visited.forEach {
                groups[it] = accountGroup
                copy.remove(it)
            }
        }
        return groups
    }

    private fun visit(visited: HashSet<String>, graph: MutableMap<String, MutableSet<String>>, node: String) {
        if (!visited.contains(node)) {
            visited.add(node)
            graph[node]?.forEach { visit(visited, graph, it) }
        }
    }

    class AccountGroup(
        val group: Set<String>,
        var mint: String? = null
    )
}
