package guru.nidi.timekeeper

import javax.swing.JFrame
import javax.swing.JOptionPane

object Dialogs {
    fun showError(message: String) {
        JFrame().apply { isAlwaysOnTop = true }.let { frame ->
            JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE)
            frame.dispose()
        }
    }
}
