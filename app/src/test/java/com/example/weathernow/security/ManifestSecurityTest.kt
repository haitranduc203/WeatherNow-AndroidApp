package com.example.weathernow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ManifestSecurityTest {

    private val androidNs = "http://schemas.android.com/apk/res/android"

    private fun resolveAppDir(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        return if (File(cwd, "src").exists()) cwd else File(cwd, "app")
    }

    private fun parseManifest(manifestFile: File): Document {
        assertTrue("Manifest file must exist: ${manifestFile.absolutePath}", manifestFile.exists())
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }
        val builder = factory.newDocumentBuilder()
        return builder.parse(manifestFile)
    }

    private fun getReceivers(doc: Document): List<Element> {
        val list = mutableListOf<Element>()
        val receiverNodes: NodeList = doc.getElementsByTagName("receiver")
        for (i in 0 until receiverNodes.length) {
            val item = receiverNodes.item(i)
            if (item is Element) {
                list.add(item)
            }
        }
        return list
    }

    private fun getAttribute(element: Element, name: String): String? {
        return if (element.hasAttributeNS(androidNs, name)) {
            element.getAttributeNS(androidNs, name)
        } else if (element.hasAttribute("android:$name")) {
            element.getAttribute("android:$name")
        } else {
            null
        }
    }

    private fun getActionNames(receiverElement: Element): Set<String> {
        val actions = mutableSetOf<String>()
        val filterNodes = receiverElement.getElementsByTagName("intent-filter")
        for (i in 0 until filterNodes.length) {
            val filter = filterNodes.item(i)
            if (filter is Element) {
                val actionNodes = filter.getElementsByTagName("action")
                for (j in 0 until actionNodes.length) {
                    val action = actionNodes.item(j)
                    if (action is Element) {
                        val name = getAttribute(action, "name")
                        if (name != null) {
                            actions.add(name)
                        }
                    }
                }
            }
        }
        return actions
    }

    @Test
    fun mainManifest_omitsTestReceiverAndActions() {
        val appDir = resolveAppDir()
        val mainManifest = File(appDir, "src/main/AndroidManifest.xml")
        val doc = parseManifest(mainManifest)
        val receivers = getReceivers(doc)

        for (receiver in receivers) {
            val name = getAttribute(receiver, "name")
            assertFalse(
                "Main manifest must not declare WeatherTestNotificationReceiver",
                name?.contains("WeatherTestNotificationReceiver") == true
            )
            val actions = getActionNames(receiver)
            assertFalse(
                "Main manifest must not declare TEST_NOTIFICATION action",
                actions.contains("com.example.weathernow.TEST_NOTIFICATION")
            )
            assertFalse(
                "Main manifest must not declare TEST_SYNC action",
                actions.contains("com.example.weathernow.TEST_SYNC")
            )
        }

        val allActionNodes = doc.getElementsByTagName("action")
        for (i in 0 until allActionNodes.length) {
            val action = allActionNodes.item(i)
            if (action is Element) {
                val name = getAttribute(action, "name")
                assertFalse(
                    "Main manifest must not contain TEST_NOTIFICATION anywhere",
                    name == "com.example.weathernow.TEST_NOTIFICATION"
                )
                assertFalse(
                    "Main manifest must not contain TEST_SYNC anywhere",
                    name == "com.example.weathernow.TEST_SYNC"
                )
            }
        }
    }

    @Test
    fun debugManifest_declaresTestReceiverAsNonExported() {
        val appDir = resolveAppDir()
        val debugManifest = File(appDir, "src/debug/AndroidManifest.xml")
        val doc = parseManifest(debugManifest)
        val receivers = getReceivers(doc)

        val testReceiver = receivers.find { receiver ->
            val name = getAttribute(receiver, "name")
            name?.contains("WeatherTestNotificationReceiver") == true
        }

        assertNotNull(
            "Debug manifest must declare WeatherTestNotificationReceiver",
            testReceiver
        )

        val exported = getAttribute(testReceiver!!, "exported")
        assertEquals(
            "WeatherTestNotificationReceiver in debug manifest must have android:exported=\"false\"",
            "false",
            exported
        )

        val actions = getActionNames(testReceiver)
        assertEquals(
            "WeatherTestNotificationReceiver must have exactly the approved actions",
            setOf(
                "com.example.weathernow.TEST_NOTIFICATION",
                "com.example.weathernow.TEST_SYNC"
            ),
            actions
        )
    }

    @Test
    fun testReceiverSource_isDebugOnly() {
        val appDir = resolveAppDir()
        val mainSource = File(appDir, "src/main/java/com/example/weathernow/core/worker/WeatherTestNotificationReceiver.kt")
        val debugSource = File(appDir, "src/debug/java/com/example/weathernow/core/worker/WeatherTestNotificationReceiver.kt")
        val releaseSource = File(appDir, "src/release/java/com/example/weathernow/core/worker/WeatherTestNotificationReceiver.kt")

        assertFalse(
            "WeatherTestNotificationReceiver.kt must NOT exist under src/main (must be debug-only)",
            mainSource.exists()
        )
        assertTrue(
            "WeatherTestNotificationReceiver.kt MUST exist under src/debug",
            debugSource.exists()
        )
        assertFalse(
            "WeatherTestNotificationReceiver.kt must NOT exist under src/release",
            releaseSource.exists()
        )

        val candidates = listOf(mainSource, debugSource, releaseSource)
        val existingCount = candidates.count { it.exists() }
        assertEquals(
            "There must be exactly one WeatherTestNotificationReceiver source file across main, debug, and release",
            1,
            existingCount
        )
    }
}
