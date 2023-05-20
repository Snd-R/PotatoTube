package org.snd.gifdecoder;

import java.util.ArrayList;
import java.util.List;

/**
 * A header object containing the number of frames in an animated GIF image as well as basic
 * metadata like width and height that can be used to decode each individual frame of the GIF
 * <p>
 * <a href="https://github.com/bumptech/glide/blob/e66d253d4c1834a7fd4e2d312f133eb8bb8daf5f/third_party/gif_decoder/src/main/java/com/bumptech/glide/gifdecoder/GifHeader.java">adapted from</a>
 *
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
public class GifHeader {

    /**
     * The "Netscape" loop count which means loop forever.
     */
    public static final int NETSCAPE_LOOP_COUNT_FOREVER = 0;
    /**
     * Indicates that this header has no "Netscape" loop count.
     */
    public static final int NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST = -1;

    int[] gct = null;
    int status = GifDecoder.STATUS_OK;
    int frameCount = 0;

    GifFrame currentFrame;
    final List<GifFrame> frames = new ArrayList<>();
    /**
     * Logical screen size: Full image width.
     */
    int width;
    /**
     * Logical screen size: Full image height.
     */
    int height;

    // 1 : global color table flag.
    boolean gctFlag;
    /**
     * Size of Global Color Table.
     * The value is already computed to be a regular number, this field doesn't store the exponent.
     */
    int gctSize;
    /**
     * Background color index into the Global/Local color table.
     */
    int bgIndex;
    /**
     * Pixel aspect ratio.
     * Factor used to compute an approximation of the aspect ratio of the pixel in the original image.
     */
    int pixelAspect;
    int bgColor;
    int loopCount = NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getNumFrames() {
        return frameCount;
    }

    /**
     * Global status code of GIF data parsing.
     */
    public int getStatus() {
        return status;
    }
}