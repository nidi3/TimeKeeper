package guru.nidi.timekeeper

import java.awt.*
import java.awt.image.BufferedImage

object TrayIcons {
    private const val SIZE = 22

    val stopped: Image = createIcon { g, size ->
        val triangleSize = 10
        val startX = (size - triangleSize) / 2 + 2
        val startY = (size - triangleSize) / 2
        g.fillPolygon(
            intArrayOf(startX, startX + triangleSize, startX),
            intArrayOf(startY, size / 2, startY + triangleSize),
            3
        )
    }

    val started: Image = createIcon { g, size ->
        val barWidth = 3
        val barHeight = 12
        val spacing = 3
        val startX = (size - barWidth * 2 - spacing) / 2
        val startY = (size - barHeight) / 2
        g.fillRect(startX, startY, barWidth, barHeight)
        g.fillRect(startX + barWidth + spacing, startY, barWidth, barHeight)
    }

    private fun createIcon(draw: (Graphics2D, Int) -> Unit) =
        BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB).also { image ->
            image.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                color = Color.WHITE
                draw(this, SIZE)
                dispose()
            }
        }
}
