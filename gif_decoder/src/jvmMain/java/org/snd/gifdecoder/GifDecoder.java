package org.snd.gifdecoder;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Shared interface for GIF decoders.
 * <p>
 * <a href="https://github.com/bumptech/glide/blob/e66d253d4c1834a7fd4e2d312f133eb8bb8daf5f/third_party/gif_decoder/src/main/java/com/bumptech/glide/gifdecoder/GifDecoder.java">adapted from</a>
 */
public interface GifDecoder {

    /**
     * File read status: No errors.
     */
    int STATUS_OK = 0;
    /**
     * File read status: Error decoding file (may be partially decoded).
     */
    int STATUS_FORMAT_ERROR = 1;
    /**
     * File read status: Unable to open source.
     */
    int STATUS_OPEN_ERROR = 2;
    /**
     * Unable to fully decode the current frame.
     */
    int STATUS_PARTIAL_DECODE = 3;
    /**
     * The total iteration count which means repeat forever.
     */
    int TOTAL_ITERATION_COUNT_FOREVER = 0;

    int getWidth();

    int getHeight();

    @Nonnull
    ByteBuffer getData();

    /**
     * Returns the current status of the decoder.
     *
     * <p> Status will update per frame to allow the caller to tell whether or not the current frame
     * was decoded successfully and/or completely. Format and open failures persist across frames.
     * </p>
     */
    int getStatus();

    /**
     * Move the animation frame counter forward.
     */
    void advance();

    /**
     * Gets display duration for specified frame.
     *
     * @param n int index of frame.
     * @return delay in milliseconds.
     */
    int getDelay(int n);

    /**
     * Gets display duration for the upcoming frame in ms.
     */
    int getNextDelay();

    /**
     * Gets the number of frames read from file.
     *
     * @return frame count.
     */
    int getFrameCount();

    /**
     * Gets the current index of the animation frame, or -1 if animation hasn't not yet started.
     *
     * @return frame index.
     */
    int getCurrentFrameIndex();

    /**
     * Resets the frame pointer to before the 0th frame, as if we'd never used this decoder to
     * decode any frames.
     */
    void resetFrameIndex();

    /**
     * Gets the "Netscape" loop count, if any. A count of 0 means repeat indefinitely.
     *
     * @return loop count if one was specified, else 1.
     * @deprecated Use {@link #getNetscapeLoopCount()} instead.
     * This method cannot distinguish whether the loop count is 1 or doesn't exist.
     */
    @Deprecated
    int getLoopCount();

    /**
     * Gets the "Netscape" loop count, if any.
     * A count of 0 ({@link GifHeader#NETSCAPE_LOOP_COUNT_FOREVER}) means repeat indefinitely.
     * It must not be a negative value.
     * <br>
     * Use {@link #getTotalIterationCount()}
     * to know how many times the animation sequence should be displayed.
     *
     * @return loop count if one was specified,
     * else -1 ({@link GifHeader#NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST}).
     */
    int getNetscapeLoopCount();

    /**
     * Gets the total count
     * which represents how many times the animation sequence should be displayed.
     * A count of 0 ({@link #TOTAL_ITERATION_COUNT_FOREVER}) means repeat indefinitely.
     * It must not be a negative value.
     * <p>
     * The total count is calculated as follows by using {@link #getNetscapeLoopCount()}.
     * This behavior is the same as most web browsers.
     *     <table border='1'>
     *         <tr class='tableSubHeadingColor'><th>{@code getNetscapeLoopCount()}</th>
     *             <th>The total count</th></tr>
     *         <tr><td>{@link GifHeader#NETSCAPE_LOOP_COUNT_FOREVER}</td>
     *             <td>{@link #TOTAL_ITERATION_COUNT_FOREVER}</td></tr>
     *         <tr><td>{@link GifHeader#NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST}</td>
     *             <td>{@code 1}</td></tr>
     *         <tr><td>{@code n (n > 0)}</td>
     *             <td>{@code n + 1}</td></tr>
     *     </table>
     * </p>
     *
     * @return total iteration count calculated from "Netscape" loop count.
     * @see <a href="https://bugs.chromium.org/p/chromium/issues/detail?id=592735#c5">Discussion about
     * the iteration count of animated GIFs (Chromium Issue 592735)</a>
     */
    int getTotalIterationCount();

    /**
     * Returns an estimated byte size for this decoder based on the data provided to {@link
     * #setData(GifHeader, byte[])}, as well as internal buffers.
     */
    int getByteSize();

    /**
     * Get the next frame in the animation sequence.
     *
     * @return Bitmap representation of frame.
     */
    @Nullable
    BufferedImage getNextFrame();

    /**
     * Reads GIF image from stream.
     *
     * @param is containing GIF file.
     * @return read status code (0 = no errors).
     */
    int read(@Nullable InputStream is, int contentLength);

    void clear();

    void setData(@Nonnull GifHeader header, @Nonnull byte[] data);

    void setData(@Nonnull GifHeader header, @Nonnull ByteBuffer buffer);

    void setData(@Nonnull GifHeader header, @Nonnull ByteBuffer buffer, int sampleSize);

    /**
     * Reads GIF image from byte array.
     *
     * @param data containing GIF file.
     * @return read status code (0 = no errors).
     */
    int read(@Nullable byte[] data);

    void setDefaultBitmapConfig(int format);
}