// Generated by jextract

package mpv;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.PathElement;
public class mpv_event_client_message {

    static final  GroupLayout $struct$LAYOUT = MemoryLayout.structLayout(
        Constants$root.C_INT$LAYOUT.withName("num_args"),
        MemoryLayout.paddingLayout(32),
        Constants$root.C_POINTER$LAYOUT.withName("args")
    ).withName("mpv_event_client_message");
    public static MemoryLayout $LAYOUT() {
        return mpv_event_client_message.$struct$LAYOUT;
    }
    static final VarHandle num_args$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("num_args"));
    public static VarHandle num_args$VH() {
        return mpv_event_client_message.num_args$VH;
    }
    public static int num_args$get(MemorySegment seg) {
        return (int)mpv_event_client_message.num_args$VH.get(seg);
    }
    public static void num_args$set( MemorySegment seg, int x) {
        mpv_event_client_message.num_args$VH.set(seg, x);
    }
    public static int num_args$get(MemorySegment seg, long index) {
        return (int)mpv_event_client_message.num_args$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void num_args$set(MemorySegment seg, long index, int x) {
        mpv_event_client_message.num_args$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle args$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("args"));
    public static VarHandle args$VH() {
        return mpv_event_client_message.args$VH;
    }
    public static MemoryAddress args$get(MemorySegment seg) {
        return (MemoryAddress)mpv_event_client_message.args$VH.get(seg);
    }
    public static void args$set( MemorySegment seg, MemoryAddress x) {
        mpv_event_client_message.args$VH.set(seg, x);
    }
    public static MemoryAddress args$get(MemorySegment seg, long index) {
        return (MemoryAddress)mpv_event_client_message.args$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void args$set(MemorySegment seg, long index, MemoryAddress x) {
        mpv_event_client_message.args$VH.set(seg.asSlice(index*sizeof()), x);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(int len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemoryAddress addr, MemorySession session) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, session); }
}

