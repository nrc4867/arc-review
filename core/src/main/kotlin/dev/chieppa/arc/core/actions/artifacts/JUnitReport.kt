package dev.chieppa.arc.core.actions.artifacts

import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/** The tests of a JUnit XML report, failures first. External entities and DTDs are refused. */
data class JUnitReport(val cases: List<JUnitCase>) {
    val failures: Int get() = cases.count { it.failed }

    companion object {
        private val LOCATION = Regex("\\(([\\w$.-]+\\.(?:kt|java|kts|scala|groovy|js|ts|tsx|jsx|py|cs|go)):(\\d+)\\)")

        /** The report in [bytes], or null when it is not one. */
        fun parse(bytes: ByteArray): JUnitReport? {
            val document = try {
                val factory = DocumentBuilderFactory.newInstance()
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                factory.isExpandEntityReferences = false
                factory.newDocumentBuilder().parse(ByteArrayInputStream(bytes))
            } catch (_: SAXException) {
                return null
            } catch (_: IOException) {
                return null
            } catch (_: ParserConfigurationException) {
                return null
            }
            val nodes = document.getElementsByTagName("testcase")
            val cases = (0 until nodes.length).map { case(nodes.item(it) as Element) }
            return JUnitReport(cases.sortedByDescending { it.failed })
        }

        private fun case(element: Element): JUnitCase {
            val problem = (element.getElementsByTagName("failure").item(0) ?: element.getElementsByTagName("error").item(0)) as Element?
            val trace = problem?.textContent.orEmpty()
            val location = LOCATION.find(trace)?.let { "${it.groupValues[1]}:${it.groupValues[2]}" }
            return JUnitCase(
                className = element.getAttribute("classname"),
                name = element.getAttribute("name"),
                seconds = element.getAttribute("time").toDoubleOrNull(),
                failure = problem?.let { it.getAttribute("message").ifEmpty { trace.lineSequence().firstOrNull().orEmpty() } },
                location = location,
                skipped = element.getElementsByTagName("skipped").length > 0,
            )
        }
    }
}
