// Generated by jextract

package mpv;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.PathElement;
public class mpv_byte_array {

    static final  GroupLayout $struct$LAYOUT = MemoryLayout.structLayout(
        Constants$root.C_POINTER$LAYOUT.withName("data"),
        Constants$root.C_LONG_LONG$LAYOUT.withName("size")
    ).withName("mpv_byte_array");
    public static MemoryLayout $LAYOUT() {
        return mpv_byte_array.$struct$LAYOUT;
    }
    static final VarHandle data$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("data"));
    public static VarHandle data$VH() {
        return mpv_byte_array.data$VH;
    }
    public static MemoryAddress data$get(MemorySegment seg) {
        return (MemoryAddress)mpv_byte_array.data$VH.get(seg);
    }
    public static void data$set( MemorySegment seg, MemoryAddress x) {
        mpv_byte_array.data$VH.set(seg, x);
    }
    public static MemoryAddress data$get(MemorySegment seg, long index) {
        return (MemoryAddress)mpv_byte_array.data$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void data$set(MemorySegment seg, long index, MemoryAddress x) {
        mpv_byte_array.data$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle size$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("size"));
    public static VarHandle size$VH() {
        return mpv_byte_array.size$VH;
    }
    public static long size$get(MemorySegment seg) {
        return (long)mpv_byte_array.size$VH.get(seg);
    }
    public static void size$set( MemorySegment seg, long x) {
        mpv_byte_array.size$VH.set(seg, x);
    }
    public static long size$get(MemorySegment seg, long index) {
        return (long)mpv_byte_array.size$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void size$set(MemorySegment seg, long index, long x) {
        mpv_byte_array.size$VH.set(seg.asSlice(index*sizeof()), x);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(int len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemoryAddress addr, MemorySession session) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, session); }
}


