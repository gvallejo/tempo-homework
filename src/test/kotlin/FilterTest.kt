import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Returns a Hierarchy instance from its formatted String version
 */
fun toHierarchy(input: String): Hierarchy {
    val nodeIds: MutableList<Int> = mutableListOf()
    val depths: MutableList<Int> = mutableListOf()

    val noBracketsInput = input.removeSurrounding("[", "]").trim()
    if(!noBracketsInput.isEmpty()) {
        noBracketsInput.split(",")
            .forEach { item -> // Applies the action to each item in the list
                val treeItems: List<String> = item.trim().split(":")
                nodeIds.add(treeItems[0].trim().toInt())
                depths.add(treeItems[1].trim().toInt())
            }
    }

    return ArrayBasedHierarchy(nodeIds.toIntArray(), depths.toIntArray())
}

class FilterTest {
    @Test
    fun testFilter() {
        val unfiltered: Hierarchy = ArrayBasedHierarchy(
            intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
            intArrayOf(0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2))
        val filteredActual: Hierarchy = unfiltered.filter { nodeId -> nodeId % 3 != 0 }
        val filteredExpected: Hierarchy = ArrayBasedHierarchy(
            intArrayOf(1, 2, 5, 8, 10, 11),
            intArrayOf(0, 1, 1, 0, 1, 2))
        assertEquals(filteredExpected.formatString(), filteredActual.formatString())
    }

    @ParameterizedTest
    @CsvSource(value = [
        // All nodes are root nodes and some pass the predicate test
        "[1:0, 6:0, 8:0]" + "|" +
        "[1:0,      8:0]",

        // All nodes are root nodes and do not pass the predicate test
        "[3:0, 6:0, 9:0]" + "|" +
        "[]",

        // All root nodes do not pass the predicate test
        "[3:0, 4:1, 5:2, 6:0, 7:1, 8:1 , 9:0, 10:1, 11:2]" + "|" +
        "[]",

        // All nodes pass the predicate test
        "[1:0, 2:1, 4:2, 5:1, 7:0, 8:1, 10:0, 11:1, 13:1, 14:2, 16:3]" + "|" +
        "[1:0, 2:1, 4:2, 5:1, 7:0, 8:1, 10:0, 11:1, 13:1, 14:2, 16:3]",

        // Empty forest
        "[]" + "|" +
        "[]",

    ], delimiter = '|')
    fun testFilter(input: String, expectedString: String) {
        // Arrange
        val inputTree: Hierarchy = toHierarchy(input)
        val expected: Hierarchy = toHierarchy(expectedString)

        // ACt
        val filteredActual: Hierarchy = inputTree.filter { nodeId -> nodeId % 3 != 0 }

        // Assert
        assertEquals(expected.formatString(), filteredActual.formatString())
    }

    @ParameterizedTest
    @CsvSource(value = [
        "[1:0, 2:1, 3:2, 4:3, 5:1, 6:0, 7:1, 8:0, 9:1, 10:1, 11:2]" + "|" +
        "[1:0, 2:1, 3:2, 4:3, 5:1, 6:0, 7:1, 8:0, 9:1            ]",

    ], delimiter = '|')
    @DisplayName("Parent of leaf node removed, leaf is also removed")
    fun testFilterParentOfLeafNodeRemovedLeafIsAlsoRemoved(input: String, expectedString: String) {
        // Arrange
        val inputTree: Hierarchy = toHierarchy(input)
        val expected: Hierarchy = toHierarchy(expectedString)

        // Act
        val filteredActual: Hierarchy = inputTree.filter { nodeId -> nodeId % 10 != 0 }

        // Assert
        assertEquals(expected.formatString(), filteredActual.formatString())
    }

    @ParameterizedTest
    @CsvSource(value = [
        "[1:0, 2:1, 3:1, 4:2, 6:2, 7:3, 8:3, 5:1, 9:2, 10:3, 11:2]" + "|" +
        "[1:0, 2:1,                          5:1, 9:2, 10:3, 11:2]",

    ], delimiter = '|')
    @DisplayName("Parent of leaf node removed, leaf is also removed")
    fun testFilterThreeNodesDepth1MiddleOneRemoved(input: String, expectedString: String) {
        // Arrange
        val inputTree: Hierarchy = toHierarchy(input)
        val expected: Hierarchy = toHierarchy(expectedString)

        // Act
        val filteredActual: Hierarchy = inputTree.filter { nodeId -> nodeId != 3 }

        // Assert
        assertEquals(expected.formatString(), filteredActual.formatString())
    }

    @ParameterizedTest
    @CsvSource(value = [
        "[1:0, 2:1, 3:2, 4:3, 5:1, 6:0, 7:1, 8:0, 9:1, 10:1, 11:2]" + "|" +
        "[]",

    ], delimiter = '|')
    @DisplayName("All nodes in depth 0 do not pass the predicate test")
    fun testFilterDepth0(input: String, expectedString: String) {
        // Arrange
        val inputTree: Hierarchy = toHierarchy(input)
        val expected: Hierarchy = toHierarchy(expectedString)

        // Act
        val filteredActual: Hierarchy = inputTree.filter { nodeId -> !listOf(1,6,8).contains(nodeId) }

        // Assert
        assertEquals(expected.formatString(), filteredActual.formatString())
    }

    @ParameterizedTest
    @CsvSource(value = [
        "[1:0, 2:1, 3:2, 4:3, 5:1, 6:0, 7:1, 8:0, 9:1, 10:1, 11:2]" + "|" +
        "[1:0,                     6:0,      8:0                 ]",

    ], delimiter = '|')
    @DisplayName("All nodes in depth 1 do not pass the predicate test")
    fun testFilterDepth1(input: String, expectedString: String) {
        // Arrange
        val inputTree: Hierarchy = toHierarchy(input)
        val expected: Hierarchy = toHierarchy(expectedString)

        // Act
        val filteredActual: Hierarchy = inputTree.filter { nodeId -> !listOf(2,5,7,9,10).contains(nodeId) }

        // Assert
        assertEquals(expected.formatString(), filteredActual.formatString())
    }

    @ParameterizedTest
    @CsvSource(value = [
        "[1:0, 2:1, 3:2, 4:3, 5:1, 6:0, 7:1, 8:0, 9:1, 10:1, 11:2]" + "|" +
        "[1:0, 2:1,           5:1, 6:0, 7:1, 8:0, 9:1, 10:1      ]",

    ], delimiter = '|')
    @DisplayName("All nodes in depth 2 do not pass the predicate test")
    fun testFilterDepth2(input: String, expectedString: String) {
        // Arrange
        val inputTree: Hierarchy = toHierarchy(input)
        val expected: Hierarchy = toHierarchy(expectedString)

        // Act
        val filteredActual: Hierarchy = inputTree.filter { nodeId -> !listOf(3,11).contains(nodeId) }

        // Assert
        assertEquals(expected.formatString(), filteredActual.formatString())
    }


    @ParameterizedTest
    @CsvSource(value = [
        "[1:0, 2:1, 3:2, 4:3, 5:1, 6:0, 7:1, 8:0, 9:1, 10:1, 11:2]" + "|" +
        "[1:0, 2:1, 3:2,      5:1, 6:0, 7:1, 8:0, 9:1, 10:1, 11:2]",

    ], delimiter = '|')
    @DisplayName("All nodes in depth 3 do not pass the predicate test")
    fun testFilterDepth3(input: String, expectedString: String) {
        //Arrange
        val inputTree: Hierarchy = toHierarchy(input)
        val expected: Hierarchy = toHierarchy(expectedString)

        // Act
        val filteredActual: Hierarchy = inputTree.filter { nodeId -> !listOf(4).contains(nodeId) }

        // Assert
        assertEquals(expected.formatString(), filteredActual.formatString())
    }

    @Test
    @DisplayName("Verify filter has the right interactions")
    fun verifyFilterHasTheRightInteractions() {
        // Arrange
        val hierarchy = mock<Hierarchy>()

        whenever(hierarchy.size).thenReturn(11)
        whenever(hierarchy.nodeId(any())).thenAnswer { inv ->
            val i = inv.getArgument<Int>(0)
            intArrayOf(1,2,3,4,5,6,7,8,9,10,11)[i]
        }
        whenever(hierarchy.depth(any())).thenAnswer { inv ->
            val i = inv.getArgument<Int>(0)
            intArrayOf(0,1,2,3,1,0,1,0,1,1,2)[i]
        }

        // Act
        hierarchy.filter { id -> id % 3 != 0 }


        // Assert
        verify(hierarchy, times(2)).size
        verify(hierarchy, times(17)).nodeId(any())
        verify(hierarchy, times(48)).depth(any())
        verifyNoMoreInteractions(hierarchy)
    }


    @Test
    @DisplayName("Verify that predicate is called by filter function")
    fun testPredicateIsCalled() {
        // Arrange
        val hierarchy: Hierarchy = ArrayBasedHierarchy(
            intArrayOf(1, 2, 3),
            intArrayOf(0, 1, 0)
        )
        val recorded = mutableListOf<Int>()

        // Act
        hierarchy.filter { id ->
            recorded += id
            id % 2 != 0
        }

        // Assert
        assertEquals(listOf(1, 2, 3), recorded)
    }

}