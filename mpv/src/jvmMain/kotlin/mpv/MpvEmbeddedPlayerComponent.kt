package mpv

import java.awt.BorderLayout
import java.awt.Canvas
import java.awt.Color
import java.awt.Component
import java.awt.event.*
import javax.swing.JPanel

class MpvEmbeddedPlayerComponent(
    private val scrollHandler: (Float) -> Unit
) : JPanel(),
    MouseListener,
    MouseMotionListener,
    MouseWheelListener,
    KeyListener {

    private val videoSurfaceComponent: Component = Canvas().apply { background = Color.black }
    val mediaPlayer: MpvPlayer = MpvPlayer(videoSurfaceComponent)

    init {
        background = Color.black
        layout = BorderLayout()
        add(videoSurfaceComponent, BorderLayout.CENTER)

        videoSurfaceComponent.addMouseListener(this)
        videoSurfaceComponent.addMouseMotionListener(this)
        videoSurfaceComponent.addMouseWheelListener(this)
        videoSurfaceComponent.addKeyListener(this)
    }

    override fun mouseClicked(e: MouseEvent?) {}

    override fun mousePressed(e: MouseEvent?) {}

    override fun mouseReleased(e: MouseEvent?) {}

    override fun mouseEntered(e: MouseEvent?) {}

    override fun mouseExited(e: MouseEvent?) {}

    override fun mouseDragged(e: MouseEvent?) {}

    override fun mouseMoved(e: MouseEvent?) {}

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        when {
            e.wheelRotation < 0 -> scrollHandler(e.scrollAmount.toFloat() * -10)
            else -> scrollHandler(e.scrollAmount.toFloat() * 10)
        }
    }

    override fun keyTyped(e: KeyEvent?) {}

    override fun keyPressed(e: KeyEvent?) {}

    override fun keyReleased(e: KeyEvent?) {}

}