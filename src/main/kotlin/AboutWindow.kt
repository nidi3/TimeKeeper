package guru.nidi.timekeeper

import java.awt.BorderLayout
import java.util.Properties
import javax.swing.*

class AboutWindow {
    private val appIcon = ImageIcon(javaClass.getResource("/TimeKeeper.iconset/icon_128x128.png"))

    private val properties by lazy {
        javaClass.getResourceAsStream("/version.properties")?.use { stream ->
            Properties().apply { load(stream) }
        } ?: Properties()
    }

    private val frame = JFrame("About").apply {
        defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        contentPane = JPanel(BorderLayout(10, 10)).apply {
            border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
            add(JLabel(appIcon), BorderLayout.WEST)
            add(Box.createVerticalBox().apply {
                add(JLabel("TimeKeeper ${properties.resolve("version")}"))
                add(JLabel(properties.resolve("commitTime")))
            }, BorderLayout.CENTER)
        }
        pack()
        setLocationRelativeTo(null)
    }

    fun show() {
        frame.apply {
            isVisible = true
            isAlwaysOnTop = true
            requestFocus()
        }
    }
}

private fun Properties.resolve(key: String) =
    getProperty(key)?.takeUnless { it.startsWith("\${") } ?: "unknown"
