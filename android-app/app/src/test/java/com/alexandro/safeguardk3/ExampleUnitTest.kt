package com.alexandro.safeguardk3

import com.alexandro.safeguardk3.ui.navigation.NavItem
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun navItems_containsExactlyFourNonNullItems() {
        val items = NavItem.items
        assertNotNull("items must not be null", items)
        assertEquals("items must contain 4 items", 4, items.size)
        items.forEachIndexed { index, item ->
            assertNotNull("Item at index $index must not be null", item)
            assertNotNull("Item route at index $index must not be null", item.route)
            assertNotNull("Item title at index $index must not be null", item.title)
            assertNotNull("Item icon at index $index must not be null", item.icon)
        }
    }
}
