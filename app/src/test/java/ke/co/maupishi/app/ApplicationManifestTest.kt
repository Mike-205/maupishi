package ke.co.maupishi.app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element

/**
 * Checks that the manifest names [MaupishiApplication]. Robolectric tests
 * override the application class, so the manifest file is read directly:
 * without this entry the app would never seed its recipes.
 */
class ApplicationManifestTest {

    @Test
    fun manifest_namesMaupishiApplicationAsTheApplicationClass() {
        val moduleRoot = listOf(File("."), File("app")).first { File(it, "src/main").isDirectory }
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(File(moduleRoot, "src/main/AndroidManifest.xml"))

        val application = document.getElementsByTagName("application").item(0) as Element
        val name = application.getAttributeNS("http://schemas.android.com/apk/res/android", "name")

        // A leading dot means "relative to the app's package".
        assertEquals(".MaupishiApplication", name)
    }
}
