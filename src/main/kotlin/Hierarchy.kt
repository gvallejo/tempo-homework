// The task:
// 1. Read and understand the Hierarchy data structure described in this file.
// 2. Implement filter() function.
// 3. Implement more test cases.
//
// The task should take 30-90 minutes.
//
// When assessing the submission, we will pay attention to:
// - correctness, efficiency, and clarity of the code;
// - the test cases.

/**
 * A `Hierarchy` stores an arbitrary _forest_ (an ordered collection of ordered trees)
 * as an array of node IDs in the order of DFS traversal, combined with a parallel array of node depths.
 *
 * Parent-child relationships are identified by the position in the array and the associated depth.
 * Each tree root has depth 0, its children have depth 1 and follow it in the array, their children have depth 2 and follow them, etc.
 *
 * Example:
 * ```
 * nodeIds: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
 * depths:  0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2
 * ```
 *
 * the forest can be visualized as follows:
 * ```
 * 1
 * - 2
 * - - 3
 * - - - 4
 * - 5
 * 6
 * - 7
 * 8
 * - 9
 * - 10
 * - - 11
 *```
 * 1 is a parent of 2 and 5, 2 is a parent of 3, etc. Note that depth is equal to the number of hyphens for each node.
 *
 * Invariants on the depths array:
 *  * Depth of the first element is 0.
 *  * If the depth of a node is `D`, the depth of the next node in the array can be:
 *      * `D + 1` if the next node is a child of this node;
 *      * `D` if the next node is a sibling of this node;
 *      * `d < D` - in this case the next node is not related to this node.
 */
interface Hierarchy {
  /** The number of nodes in the hierarchy. */
  val size: Int

  /**
   * Returns the unique ID of the node identified by the hierarchy index. The depth for this node will be `depth(index)`.
   * @param index must be non-negative and less than [size]
   * */
  fun nodeId(index: Int): Int

  /**
   * Returns the depth of the node identified by the hierarchy index. The unique ID for this node will be `nodeId(index)`.
   * @param index must be non-negative and less than [size]
   * */
  fun depth(index: Int): Int

  fun formatString(): String {
    return (0 until size).joinToString(
      separator = ", ",
      prefix = "[",
      postfix = "]"
    ) { i -> "${nodeId(i)}:${depth(i)}" }
  }
}

/**
 * A node is present in the filtered hierarchy iff its node ID passes the predicate and all of its ancestors pass it as well.
 */
fun Hierarchy.filter(nodeIdPredicate: (Int) -> Boolean): Hierarchy {
    val resultForestNodeIds: MutableList<Int> = mutableListOf()
    val resultForestDepths: MutableList<Int> = mutableListOf()

    val filteredTreeNodeIds: MutableList<Int> = mutableListOf()
    val filteredTreeDepths: MutableList<Int> = mutableListOf()

    // Since nodes with depth D can only be children of the most right node with depth D - 1 (as we traverse the
    // tree in a DFS way) We keep track of the presence of the resulting most right nodes by depth after applying
    // the filtering rules.
    // This is useful to check if a node that passes the filter has a parent that also passed the filter.
    val isMostRightNodeWithDepthPresent = BooleanArray(size)

    for(index in 0..<size) {
        if(isRootNode(index)) {
            // When we find a new root node we flush the filtered Tree into the resulting Forest
            // in preparation to filter a new Tree
            flush(filteredTreeNodeIds, resultForestNodeIds)
            flush(filteredTreeDepths, resultForestDepths)
        }

        isMostRightNodeWithDepthPresent[depth(index)] = false
        if (nodeIdPredicate(nodeId(index)) && (isRootNode(index) || hasParentInTree(index, isMostRightNodeWithDepthPresent))) {
            isMostRightNodeWithDepthPresent[depth(index)] = true
            filteredTreeNodeIds.add(nodeId(index))
            filteredTreeDepths.add(depth(index))
        }
    }

    // Flush the contents of the filtered tree into the resulting forest
    flush(filteredTreeNodeIds, resultForestNodeIds)
    flush(filteredTreeDepths, resultForestDepths)

  return ArrayBasedHierarchy(resultForestNodeIds.toIntArray(), resultForestDepths.toIntArray())
}

/**
 * Copies all elements of a source collection into a target collection, then clears the source.
 */
private fun Hierarchy.flush(sourceCollection: MutableList<Int>, targetCollection: MutableList<Int>) {
    targetCollection.addAll(sourceCollection)
    sourceCollection.clear()
}

/**
 * Determines if the node corresponding to the index is a root node
 */
private fun Hierarchy.isRootNode(index: Int): Boolean {
    return depth(index) == 0
}

/**
 * Determines if the node corresponding to the index has a parent in the tree passed as argument
 */
private fun Hierarchy.hasParentInTree(index: Int, isMostRightNodeWithDepthPresent: BooleanArray): Boolean {
    val parentDepth: Int = depth(index) - 1
    return isMostRightNodeWithDepthPresent[parentDepth]
}


class ArrayBasedHierarchy(
  private val myNodeIds: IntArray,
  private val myDepths: IntArray,
) : Hierarchy {
  override val size: Int = myDepths.size

  override fun nodeId(index: Int): Int = myNodeIds[index]

  override fun depth(index: Int): Int = myDepths[index]
}


