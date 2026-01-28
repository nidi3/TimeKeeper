package guru.nidi.timekeeper

import java.awt.BorderLayout
import java.util.Properties
import javax.swing.*

class AboutWindow {
    private val appIcon = ImageIcon(javaClass.getResource("/TimeKeeper.iconset/icon_128x128.png"))

    private val version by lazy {
        javaClass.getResourceAsStream("/version.properties")?.use { stream ->
            Properties().apply { load(stream) }.getProperty("version")
        } ?: "unknown"
    }

    private val frame = JFrame("About").apply {
        defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        contentPane = JPanel(BorderLayout(10, 10)).apply {
            border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
            add(JLabel(appIcon), BorderLayout.WEST)
            add(JLabel("TimeKeeper $version"), BorderLayout.CENTER)
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
