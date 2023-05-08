// Generated by jextract

package locale;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.PathElement;
public class lconv {

    static final  GroupLayout $struct$LAYOUT = MemoryLayout.structLayout(
        Constants$root.C_POINTER$LAYOUT.withName("decimal_point"),
        Constants$root.C_POINTER$LAYOUT.withName("thousands_sep"),
        Constants$root.C_POINTER$LAYOUT.withName("grouping"),
        Constants$root.C_POINTER$LAYOUT.withName("int_curr_symbol"),
        Constants$root.C_POINTER$LAYOUT.withName("currency_symbol"),
        Constants$root.C_POINTER$LAYOUT.withName("mon_decimal_point"),
        Constants$root.C_POINTER$LAYOUT.withName("mon_thousands_sep"),
        Constants$root.C_POINTER$LAYOUT.withName("mon_grouping"),
        Constants$root.C_POINTER$LAYOUT.withName("positive_sign"),
        Constants$root.C_POINTER$LAYOUT.withName("negative_sign"),
        Constants$root.C_CHAR$LAYOUT.withName("int_frac_digits"),
        Constants$root.C_CHAR$LAYOUT.withName("frac_digits"),
        Constants$root.C_CHAR$LAYOUT.withName("p_cs_precedes"),
        Constants$root.C_CHAR$LAYOUT.withName("p_sep_by_space"),
        Constants$root.C_CHAR$LAYOUT.withName("n_cs_precedes"),
        Constants$root.C_CHAR$LAYOUT.withName("n_sep_by_space"),
        Constants$root.C_CHAR$LAYOUT.withName("p_sign_posn"),
        Constants$root.C_CHAR$LAYOUT.withName("n_sign_posn"),
        Constants$root.C_CHAR$LAYOUT.withName("int_p_cs_precedes"),
        Constants$root.C_CHAR$LAYOUT.withName("int_p_sep_by_space"),
        Constants$root.C_CHAR$LAYOUT.withName("int_n_cs_precedes"),
        Constants$root.C_CHAR$LAYOUT.withName("int_n_sep_by_space"),
        Constants$root.C_CHAR$LAYOUT.withName("int_p_sign_posn"),
        Constants$root.C_CHAR$LAYOUT.withName("int_n_sign_posn"),
        MemoryLayout.paddingLayout(16)
    ).withName("lconv");
    public static MemoryLayout $LAYOUT() {
        return lconv.$struct$LAYOUT;
    }
    static final VarHandle decimal_point$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("decimal_point"));
    public static VarHandle decimal_point$VH() {
        return lconv.decimal_point$VH;
    }
    public static MemoryAddress decimal_point$get(MemorySegment seg) {
        return (MemoryAddress)lconv.decimal_point$VH.get(seg);
    }
    public static void decimal_point$set( MemorySegment seg, MemoryAddress x) {
        lconv.decimal_point$VH.set(seg, x);
    }
    public static MemoryAddress decimal_point$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.decimal_point$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void decimal_point$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.decimal_point$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle thousands_sep$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("thousands_sep"));
    public static VarHandle thousands_sep$VH() {
        return lconv.thousands_sep$VH;
    }
    public static MemoryAddress thousands_sep$get(MemorySegment seg) {
        return (MemoryAddress)lconv.thousands_sep$VH.get(seg);
    }
    public static void thousands_sep$set( MemorySegment seg, MemoryAddress x) {
        lconv.thousands_sep$VH.set(seg, x);
    }
    public static MemoryAddress thousands_sep$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.thousands_sep$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void thousands_sep$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.thousands_sep$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle grouping$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("grouping"));
    public static VarHandle grouping$VH() {
        return lconv.grouping$VH;
    }
    public static MemoryAddress grouping$get(MemorySegment seg) {
        return (MemoryAddress)lconv.grouping$VH.get(seg);
    }
    public static void grouping$set( MemorySegment seg, MemoryAddress x) {
        lconv.grouping$VH.set(seg, x);
    }
    public static MemoryAddress grouping$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.grouping$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void grouping$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.grouping$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle int_curr_symbol$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("int_curr_symbol"));
    public static VarHandle int_curr_symbol$VH() {
        return lconv.int_curr_symbol$VH;
    }
    public static MemoryAddress int_curr_symbol$get(MemorySegment seg) {
        return (MemoryAddress)lconv.int_curr_symbol$VH.get(seg);
    }
    public static void int_curr_symbol$set( MemorySegment seg, MemoryAddress x) {
        lconv.int_curr_symbol$VH.set(seg, x);
    }
    public static MemoryAddress int_curr_symbol$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.int_curr_symbol$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void int_curr_symbol$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.int_curr_symbol$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle currency_symbol$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("currency_symbol"));
    public static VarHandle currency_symbol$VH() {
        return lconv.currency_symbol$VH;
    }
    public static MemoryAddress currency_symbol$get(MemorySegment seg) {
        return (MemoryAddress)lconv.currency_symbol$VH.get(seg);
    }
    public static void currency_symbol$set( MemorySegment seg, MemoryAddress x) {
        lconv.currency_symbol$VH.set(seg, x);
    }
    public static MemoryAddress currency_symbol$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.currency_symbol$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void currency_symbol$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.currency_symbol$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle mon_decimal_point$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("mon_decimal_point"));
    public static VarHandle mon_decimal_point$VH() {
        return lconv.mon_decimal_point$VH;
    }
    public static MemoryAddress mon_decimal_point$get(MemorySegment seg) {
        return (MemoryAddress)lconv.mon_decimal_point$VH.get(seg);
    }
    public static void mon_decimal_point$set( MemorySegment seg, MemoryAddress x) {
        lconv.mon_decimal_point$VH.set(seg, x);
    }
    public static MemoryAddress mon_decimal_point$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.mon_decimal_point$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void mon_decimal_point$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.mon_decimal_point$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle mon_thousands_sep$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("mon_thousands_sep"));
    public static VarHandle mon_thousands_sep$VH() {
        return lconv.mon_thousands_sep$VH;
    }
    public static MemoryAddress mon_thousands_sep$get(MemorySegment seg) {
        return (MemoryAddress)lconv.mon_thousands_sep$VH.get(seg);
    }
    public static void mon_thousands_sep$set( MemorySegment seg, MemoryAddress x) {
        lconv.mon_thousands_sep$VH.set(seg, x);
    }
    public static MemoryAddress mon_thousands_sep$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.mon_thousands_sep$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void mon_thousands_sep$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.mon_thousands_sep$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle mon_grouping$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("mon_grouping"));
    public static VarHandle mon_grouping$VH() {
        return lconv.mon_grouping$VH;
    }
    public static MemoryAddress mon_grouping$get(MemorySegment seg) {
        return (MemoryAddress)lconv.mon_grouping$VH.get(seg);
    }
    public static void mon_grouping$set( MemorySegment seg, MemoryAddress x) {
        lconv.mon_grouping$VH.set(seg, x);
    }
    public static MemoryAddress mon_grouping$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.mon_grouping$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void mon_grouping$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.mon_grouping$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle positive_sign$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("positive_sign"));
    public static VarHandle positive_sign$VH() {
        return lconv.positive_sign$VH;
    }
    public static MemoryAddress positive_sign$get(MemorySegment seg) {
        return (MemoryAddress)lconv.positive_sign$VH.get(seg);
    }
    public static void positive_sign$set( MemorySegment seg, MemoryAddress x) {
        lconv.positive_sign$VH.set(seg, x);
    }
    public static MemoryAddress positive_sign$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.positive_sign$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void positive_sign$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.positive_sign$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle negative_sign$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("negative_sign"));
    public static VarHandle negative_sign$VH() {
        return lconv.negative_sign$VH;
    }
    public static MemoryAddress negative_sign$get(MemorySegment seg) {
        return (MemoryAddress)lconv.negative_sign$VH.get(seg);
    }
    public static void negative_sign$set( MemorySegment seg, MemoryAddress x) {
        lconv.negative_sign$VH.set(seg, x);
    }
    public static MemoryAddress negative_sign$get(MemorySegment seg, long index) {
        return (MemoryAddress)lconv.negative_sign$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void negative_sign$set(MemorySegment seg, long index, MemoryAddress x) {
        lconv.negative_sign$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle int_frac_digits$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("int_frac_digits"));
    public static VarHandle int_frac_digits$VH() {
        return lconv.int_frac_digits$VH;
    }
    public static byte int_frac_digits$get(MemorySegment seg) {
        return (byte)lconv.int_frac_digits$VH.get(seg);
    }
    public static void int_frac_digits$set( MemorySegment seg, byte x) {
        lconv.int_frac_digits$VH.set(seg, x);
    }
    public static byte int_frac_digits$get(MemorySegment seg, long index) {
        return (byte)lconv.int_frac_digits$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void int_frac_digits$set(MemorySegment seg, long index, byte x) {
        lconv.int_frac_digits$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle frac_digits$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("frac_digits"));
    public static VarHandle frac_digits$VH() {
        return lconv.frac_digits$VH;
    }
    public static byte frac_digits$get(MemorySegment seg) {
        return (byte)lconv.frac_digits$VH.get(seg);
    }
    public static void frac_digits$set( MemorySegment seg, byte x) {
        lconv.frac_digits$VH.set(seg, x);
    }
    public static byte frac_digits$get(MemorySegment seg, long index) {
        return (byte)lconv.frac_digits$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void frac_digits$set(MemorySegment seg, long index, byte x) {
        lconv.frac_digits$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle p_cs_precedes$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("p_cs_precedes"));
    public static VarHandle p_cs_precedes$VH() {
        return lconv.p_cs_precedes$VH;
    }
    public static byte p_cs_precedes$get(MemorySegment seg) {
        return (byte)lconv.p_cs_precedes$VH.get(seg);
    }
    public static void p_cs_precedes$set( MemorySegment seg, byte x) {
        lconv.p_cs_precedes$VH.set(seg, x);
    }
    public static byte p_cs_precedes$get(MemorySegment seg, long index) {
        return (byte)lconv.p_cs_precedes$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void p_cs_precedes$set(MemorySegment seg, long index, byte x) {
        lconv.p_cs_precedes$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle p_sep_by_space$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("p_sep_by_space"));
    public static VarHandle p_sep_by_space$VH() {
        return lconv.p_sep_by_space$VH;
    }
    public static byte p_sep_by_space$get(MemorySegment seg) {
        return (byte)lconv.p_sep_by_space$VH.get(seg);
    }
    public static void p_sep_by_space$set( MemorySegment seg, byte x) {
        lconv.p_sep_by_space$VH.set(seg, x);
    }
    public static byte p_sep_by_space$get(MemorySegment seg, long index) {
        return (byte)lconv.p_sep_by_space$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void p_sep_by_space$set(MemorySegment seg, long index, byte x) {
        lconv.p_sep_by_space$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle n_cs_precedes$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("n_cs_precedes"));
    public static VarHandle n_cs_precedes$VH() {
        return lconv.n_cs_precedes$VH;
    }
    public static byte n_cs_precedes$get(MemorySegment seg) {
        return (byte)lconv.n_cs_precedes$VH.get(seg);
    }
    public static void n_cs_precedes$set( MemorySegment seg, byte x) {
        lconv.n_cs_precedes$VH.set(seg, x);
    }
    public static byte n_cs_precedes$get(MemorySegment seg, long index) {
        return (byte)lconv.n_cs_precedes$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void n_cs_precedes$set(MemorySegment seg, long index, byte x) {
        lconv.n_cs_precedes$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle n_sep_by_space$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("n_sep_by_space"));
    public static VarHandle n_sep_by_space$VH() {
        return lconv.n_sep_by_space$VH;
    }
    public static byte n_sep_by_space$get(MemorySegment seg) {
        return (byte)lconv.n_sep_by_space$VH.get(seg);
    }
    public static void n_sep_by_space$set( MemorySegment seg, byte x) {
        lconv.n_sep_by_space$VH.set(seg, x);
    }
    public static byte n_sep_by_space$get(MemorySegment seg, long index) {
        return (byte)lconv.n_sep_by_space$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void n_sep_by_space$set(MemorySegment seg, long index, byte x) {
        lconv.n_sep_by_space$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle p_sign_posn$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("p_sign_posn"));
    public static VarHandle p_sign_posn$VH() {
        return lconv.p_sign_posn$VH;
    }
    public static byte p_sign_posn$get(MemorySegment seg) {
        return (byte)lconv.p_sign_posn$VH.get(seg);
    }
    public static void p_sign_posn$set( MemorySegment seg, byte x) {
        lconv.p_sign_posn$VH.set(seg, x);
    }
    public static byte p_sign_posn$get(MemorySegment seg, long index) {
        return (byte)lconv.p_sign_posn$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void p_sign_posn$set(MemorySegment seg, long index, byte x) {
        lconv.p_sign_posn$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle n_sign_posn$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("n_sign_posn"));
    public static VarHandle n_sign_posn$VH() {
        return lconv.n_sign_posn$VH;
    }
    public static byte n_sign_posn$get(MemorySegment seg) {
        return (byte)lconv.n_sign_posn$VH.get(seg);
    }
    public static void n_sign_posn$set( MemorySegment seg, byte x) {
        lconv.n_sign_posn$VH.set(seg, x);
    }
    public static byte n_sign_posn$get(MemorySegment seg, long index) {
        return (byte)lconv.n_sign_posn$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void n_sign_posn$set(MemorySegment seg, long index, byte x) {
        lconv.n_sign_posn$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle int_p_cs_precedes$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("int_p_cs_precedes"));
    public static VarHandle int_p_cs_precedes$VH() {
        return lconv.int_p_cs_precedes$VH;
    }
    public static byte int_p_cs_precedes$get(MemorySegment seg) {
        return (byte)lconv.int_p_cs_precedes$VH.get(seg);
    }
    public static void int_p_cs_precedes$set( MemorySegment seg, byte x) {
        lconv.int_p_cs_precedes$VH.set(seg, x);
    }
    public static byte int_p_cs_precedes$get(MemorySegment seg, long index) {
        return (byte)lconv.int_p_cs_precedes$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void int_p_cs_precedes$set(MemorySegment seg, long index, byte x) {
        lconv.int_p_cs_precedes$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle int_p_sep_by_space$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("int_p_sep_by_space"));
    public static VarHandle int_p_sep_by_space$VH() {
        return lconv.int_p_sep_by_space$VH;
    }
    public static byte int_p_sep_by_space$get(MemorySegment seg) {
        return (byte)lconv.int_p_sep_by_space$VH.get(seg);
    }
    public static void int_p_sep_by_space$set( MemorySegment seg, byte x) {
        lconv.int_p_sep_by_space$VH.set(seg, x);
    }
    public static byte int_p_sep_by_space$get(MemorySegment seg, long index) {
        return (byte)lconv.int_p_sep_by_space$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void int_p_sep_by_space$set(MemorySegment seg, long index, byte x) {
        lconv.int_p_sep_by_space$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle int_n_cs_precedes$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("int_n_cs_precedes"));
    public static VarHandle int_n_cs_precedes$VH() {
        return lconv.int_n_cs_precedes$VH;
    }
    public static byte int_n_cs_precedes$get(MemorySegment seg) {
        return (byte)lconv.int_n_cs_precedes$VH.get(seg);
    }
    public static void int_n_cs_precedes$set( MemorySegment seg, byte x) {
        lconv.int_n_cs_precedes$VH.set(seg, x);
    }
    public static byte int_n_cs_precedes$get(MemorySegment seg, long index) {
        return (byte)lconv.int_n_cs_precedes$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void int_n_cs_precedes$set(MemorySegment seg, long index, byte x) {
        lconv.int_n_cs_precedes$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle int_n_sep_by_space$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("int_n_sep_by_space"));
    public static VarHandle int_n_sep_by_space$VH() {
        return lconv.int_n_sep_by_space$VH;
    }
    public static byte int_n_sep_by_space$get(MemorySegment seg) {
        return (byte)lconv.int_n_sep_by_space$VH.get(seg);
    }
    public static void int_n_sep_by_space$set( MemorySegment seg, byte x) {
        lconv.int_n_sep_by_space$VH.set(seg, x);
    }
    public static byte int_n_sep_by_space$get(MemorySegment seg, long index) {
        return (byte)lconv.int_n_sep_by_space$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void int_n_sep_by_space$set(MemorySegment seg, long index, byte x) {
        lconv.int_n_sep_by_space$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle int_p_sign_posn$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("int_p_sign_posn"));
    public static VarHandle int_p_sign_posn$VH() {
        return lconv.int_p_sign_posn$VH;
    }
    public static byte int_p_sign_posn$get(MemorySegment seg) {
        return (byte)lconv.int_p_sign_posn$VH.get(seg);
    }
    public static void int_p_sign_posn$set( MemorySegment seg, byte x) {
        lconv.int_p_sign_posn$VH.set(seg, x);
    }
    public static byte int_p_sign_posn$get(MemorySegment seg, long index) {
        return (byte)lconv.int_p_sign_posn$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void int_p_sign_posn$set(MemorySegment seg, long index, byte x) {
        lconv.int_p_sign_posn$VH.set(seg.asSlice(index*sizeof()), x);
    }
    static final VarHandle int_n_sign_posn$VH = $struct$LAYOUT.varHandle(PathElement.groupElement("int_n_sign_posn"));
    public static VarHandle int_n_sign_posn$VH() {
        return lconv.int_n_sign_posn$VH;
    }
    public static byte int_n_sign_posn$get(MemorySegment seg) {
        return (byte)lconv.int_n_sign_posn$VH.get(seg);
    }
    public static void int_n_sign_posn$set( MemorySegment seg, byte x) {
        lconv.int_n_sign_posn$VH.set(seg, x);
    }
    public static byte int_n_sign_posn$get(MemorySegment seg, long index) {
        return (byte)lconv.int_n_sign_posn$VH.get(seg.asSlice(index*sizeof()));
    }
    public static void int_n_sign_posn$set(MemorySegment seg, long index, byte x) {
        lconv.int_n_sign_posn$VH.set(seg.asSlice(index*sizeof()), x);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(int len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemoryAddress addr, MemorySession session) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, session); }
}

