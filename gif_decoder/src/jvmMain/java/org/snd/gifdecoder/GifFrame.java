package org.snd.gifdecoder;

/**
 * Inner model class housing metadata for each frame.
 * <p>
 * <a href="https://github.com/bumptech/glide/blob/e66d253d4c1834a7fd4e2d312f133eb8bb8daf5f/third_party/gif_decoder/src/main/java/com/bumptech/glide/gifdecoder/GifFrame.java">adapted from</a>
 *
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
class GifFrame {
    /**
     * GIF Disposal Method meaning take no action.
     * <p><b>GIF89a</b>: <i>No disposal specified.
     * The decoder is not required to take any action.</i></p>
     */
    static final int DISPOSAL_UNSPECIFIED = 0;
    /**
     * GIF Disposal Method meaning leave canvas from previous frame.
     * <p><b>GIF89a</b>: <i>Do not dispose.
     * The graphic is to be left in place.</i></p>
     */
    static final int DISPOSAL_NONE = 1;
    /**
     * GIF Disposal Method meaning clear canvas to background color.
     * <p><b>GIF89a</b>: <i>Restore to background color.
     * The area used by the graphic must be restored to the background color.</i></p>
     */
    static final int DISPOSAL_BACKGROUND = 2;
    /**
     * GIF Disposal Method meaning clear canvas to frame before last.
     * <p><b>GIF89a</b>: <i>Restore to previous.
     * The decoder is required to restore the area overwritten by the graphic
     * with what was there prior to rendering the graphic.</i></p>
     */
    static final int DISPOSAL_PREVIOUS = 3;

    int ix, iy, iw, ih;
    /**
     * Control Flag.
     */
    boolean interlace;
    /**
     * Control Flag.
     */
    boolean transparency;
    /**
     * Disposal Method.
     */
    int dispose;
    /**
     * Transparency Index.
     */
    int transIndex;
    /**
     * Delay, in milliseconds, to next frame.
     */
    int delay;
    /**
     * Index in the raw buffer where we need to start reading to decode.
     */
    int bufferFrameStart;
    /**
     * Local Color Table.
     */
//    @ColorInt
    int[] lct;
}