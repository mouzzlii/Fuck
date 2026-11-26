package de.trakuna.app

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataWedgeIntentFilterInstrumentedTest {
    @Test
    fun filterMatchesTheConfiguredDataWedgeActionAndDefaultCategory() {
        val filter = createDataWedgeIntentFilter()
        val intent = Intent(DATAWEDGE_ACTION).addCategory(Intent.CATEGORY_DEFAULT)

        assertTrue(filter.hasAction(DATAWEDGE_ACTION))
        assertTrue(filter.hasCategory(Intent.CATEGORY_DEFAULT))
        assertNull(filter.matchCategories(intent.categories))
    }

    @Test
    fun filterRejectsAnUnexpectedIntentCategory() {
        val filter = createDataWedgeIntentFilter()
        val intent = Intent(DATAWEDGE_ACTION).addCategory("de.trakuna.app.UNEXPECTED")

        assertFalse(filter.hasCategory("de.trakuna.app.UNEXPECTED"))
        assertTrue(filter.matchCategories(intent.categories) != null)
    }
}
