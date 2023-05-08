package mpv

import com.sun.jna.Native
import locale.CLocale
import mpv.MpvClient.*
import mu.KotlinLogging
import java.awt.Component
import java.lang.foreign.Addressable
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

class MpvPlayer(
    private val videoSurface: Component
) {

    private val memorySession: MemorySession = MemorySession.openShared()
    private val mpvHandle: Addressable
    private var isInitialized: Boolean = false
    private var eventLoopThread: Thread? = null

    private var onVideoStart: ((MpvPlayer) -> Unit)? = null

    init {
        CLocale.setlocale(CLocale.LC_NUMERIC(), memorySession.allocateUtf8String("C"))
        mpvHandle = mpv_create()

        if (mpvHandle.get(C_INT, 0) == 0) {
            memorySession.close()
            throw IllegalStateException("Failed to create mpv instance")
        }

        mpv_request_log_messages(mpvHandle, memorySession.allocateUtf8String("debug"))
        startEventLoop()
    }


    fun play(url: String) {
        MemorySession.openConfined().use { memorySession ->
            if (!isInitialized) initialize(getComponentId(videoSurface))
            val seqArr = MemoryLayout.sequenceLayout(3, C_POINTER)
            val array = memorySession.allocate(seqArr)
            array.setAtIndex(C_POINTER, 0, memorySession.allocateUtf8String("loadfile"))
            array.setAtIndex(C_POINTER, 1, memorySession.allocateUtf8String(url))
            mpv_command_async(mpvHandle, 0, array)
        }
    }

    fun seek(seekToInMillis: Long) {
        MemorySession.openConfined().use { memorySession ->
            mpv_set_property(
                mpvHandle,
                memorySession.allocateUtf8String("time-pos"),
                MPV_FORMAT_DOUBLE(),
                memorySession.allocate(C_DOUBLE, seekToInMillis / 1000.0)
            )
        }
    }

    fun pause() {
        MemorySession.openConfined().use { memorySession ->
            mpv_set_property(
                mpvHandle,
                memorySession.allocateUtf8String("pause"),
                MPV_FORMAT_FLAG(),
                memorySession.allocate(C_INT, 1)
            )
        }
    }

    fun unPause() {
        MemorySession.openConfined().use { memorySession ->
            mpv_set_property(
                mpvHandle,
                memorySession.allocateUtf8String("pause"),
                MPV_FORMAT_FLAG(),
                memorySession.allocate(C_POINTER)
            )
        }
    }

    fun duration(): Long {
        MemorySession.openConfined().use { memorySession ->
            val doublePointer = memorySession.allocate(C_POINTER)
            val res = mpv_get_property(
                mpvHandle,
                memorySession.allocateUtf8String("duration"),
                MPV_FORMAT_DOUBLE(),
                doublePointer
            )
            return if (res == 0) {
                val duration = doublePointer.get(C_DOUBLE, 0)
                (duration * 1000).toLong()
            } else 0
        }
    }

    fun unMute() {
        MemorySession.openConfined().use { memorySession ->
            mpv_set_property(
                mpvHandle,
                memorySession.allocateUtf8String("mute"),
                MPV_FORMAT_FLAG(),
                memorySession.allocate(C_POINTER)
            )
        }

    }

    fun mute() {
        MemorySession.openConfined().use { memorySession ->
            mpv_set_property(
                mpvHandle,
                memorySession.allocateUtf8String("mute"),
                MPV_FORMAT_FLAG(),
                memorySession.allocate(C_INT, 1)
            )
        }
    }

    fun setVolume(volume: Int) {
        MemorySession.openConfined().use { memorySession ->
            mpv_set_property(
                mpvHandle,
                memorySession.allocateUtf8String("volume"),
                MPV_FORMAT_INT64(),
                memorySession.allocate(C_LONG, volume.toLong())
            )
        }
    }

    fun currentPosition(): Long {
        MemorySession.openConfined().use { memorySession ->
            val doublePointer = memorySession.allocate(C_POINTER)
            val res = mpv_get_property(
                mpvHandle,
                memorySession.allocateUtf8String("time-pos"),
                MPV_FORMAT_DOUBLE(),
                doublePointer
            )

            return if (res == 0) {
                val position = doublePointer.get(C_DOUBLE, 0)
                (position * 1000).toLong()
            } else 0
        }
    }

    fun setOnVideoLoadedCallback(callback: (player: MpvPlayer) -> Unit) {
        this.onVideoStart = callback
    }


    private fun stop() {
        val sequence = MemoryLayout.sequenceLayout(2, C_POINTER)
        val command = memorySession.allocate(sequence)
        command.setAtIndex(C_POINTER, 0, memorySession.allocateUtf8String("stop"))
        mpv_command(mpvHandle, command)
    }

    private fun quit() {
        MemorySession.openConfined().use { memorySession ->
            val command = memorySession.allocate(MemoryLayout.sequenceLayout(2, C_POINTER))
            command.setAtIndex(C_POINTER, 0, memorySession.allocateUtf8String("quit"))
            mpv_command(mpvHandle, command)
        }
    }

    private fun initialize(wid: Long) {
        MemorySession.openConfined().use { memorySession ->
            val res = mpv_set_option(
                mpvHandle,
                memorySession.allocateUtf8String("wid"),
                MPV_FORMAT_INT64(),
                memorySession.allocate(C_LONG, wid)
            )
            mpv_set_option(
                mpvHandle,
                memorySession.allocateUtf8String("input-vo-keyboard"),
                MPV_FORMAT_FLAG(),
                memorySession.allocate(C_INT, 1)
            )

            mpv_set_option(
                mpvHandle,
                memorySession.allocateUtf8String("input-cursor"),
                MPV_FORMAT_FLAG(),
                memorySession.allocate(C_INT, 0)
            )
            if (res != 0) {
                throw IllegalStateException("Can't set wid: $res")
            }

            val initializeResult = mpv_initialize(mpvHandle)
            if (initializeResult != 0)
                throw IllegalStateException("Can't initialize MPV. Error code: $initializeResult")

//            mpv_observe_property(mpvHandle, 0, memorySession.allocateUtf8String("time-pos"), MPV_FORMAT_DOUBLE())

            isInitialized = true
        }
    }

    private fun getComponentId(component: Component): Long {
        return Native.getComponentID(component)
    }

    private fun startEventLoop() {
        this.eventLoopThread = thread(name = "MPV-Event-Loop") {
            MemorySession.openConfined().use { memorySession ->
                while (true) {
                    val event = mpv_event.ofAddress(mpv_wait_event(mpvHandle, 1.0), memorySession)

                    when (mpv_event.`event_id$get`(event)) {
//                        MPV_EVENT_PROPERTY_CHANGE() -> handlePropChangeEvent(event)
                        MPV_EVENT_LOG_MESSAGE() -> handleLogEvent(event)
                        MPV_EVENT_FILE_LOADED() -> handleFileLoadedEvent(event)
                    }

                    if (Thread.interrupted()) break
                }
            }
        }
    }

    private fun handlePropChangeEvent(event: MemorySegment) {
        val eventProperty = mpv_event_property.ofAddress(mpv_event.`data$get`(event), memorySession)
        val format = mpv_event_property.`format$get`(eventProperty)
        when (mpv_event_property.`name$get`(event).getUtf8String(0)) {
            "time-pos" -> {
                if (format == MPV_FORMAT_DOUBLE()) {
                    val currentTime = mpv_event_property.`data$get`(eventProperty).get(C_DOUBLE, 0)
                }
            }
        }
    }

    private fun handleLogEvent(event: MemorySegment) {
        val data = mpv_event.`data$get`(event)
        val logMessage = mpv_event_log_message.ofAddress(data, memorySession)
        val message = mpv_event_log_message.`text$get`(logMessage).getUtf8String(0).trimEnd()
        logger.debug { message }
    }

    private fun handleFileLoadedEvent(event: MemorySegment) {
        onVideoStart?.let { it(this) }
    }

    fun release() {
        quit()
        eventLoopThread?.interrupt()
        eventLoopThread?.join()
        mpv_destroy(mpvHandle)
        memorySession.close()
    }
}
