// Generated by jextract

package mpv;

import java.lang.foreign.*;

public class mpv_event_command {

    static final  GroupLayout $struct$LAYOUT = MemoryLayout.structLayout(
        MemoryLayout.structLayout(
            MemoryLayout.unionLayout(
                Constants$root.C_POINTER$LAYOUT.withName("string"),
                Constants$root.C_INT$LAYOUT.withName("flag"),
                Constants$root.C_LONG_LONG$LAYOUT.withName("int64"),
                Constants$root.C_DOUBLE$LAYOUT.withName("double_"),
                Constants$root.C_POINTER$LAYOUT.withName("list"),
                Constants$root.C_POINTER$LAYOUT.withName("ba")
            ).withName("u"),
            Constants$root.C_INT$LAYOUT.withName("format"),
            MemoryLayout.paddingLayout(32)
        ).withName("result")
    ).withName("mpv_event_command");
    public static MemoryLayout $LAYOUT() {
        return mpv_event_command.$struct$LAYOUT;
    }
    public static MemorySegment result$slice(MemorySegment seg) {
        return seg.asSlice(0, 16);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(int len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemoryAddress addr, MemorySession session) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, session); }
}

