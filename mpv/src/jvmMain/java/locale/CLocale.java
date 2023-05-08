// Generated by jextract

package locale;

import java.lang.foreign.Addressable;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;
public class CLocale  {

    /* package-private */ CLocale() {}
    public static OfByte C_CHAR = Constants$root.C_CHAR$LAYOUT;
    public static OfShort C_SHORT = Constants$root.C_SHORT$LAYOUT;
    public static OfInt C_INT = Constants$root.C_INT$LAYOUT;
    public static OfLong C_LONG = Constants$root.C_LONG_LONG$LAYOUT;
    public static OfLong C_LONG_LONG = Constants$root.C_LONG_LONG$LAYOUT;
    public static OfFloat C_FLOAT = Constants$root.C_FLOAT$LAYOUT;
    public static OfDouble C_DOUBLE = Constants$root.C_DOUBLE$LAYOUT;
    public static OfAddress C_POINTER = Constants$root.C_POINTER$LAYOUT;
    public static int _LOCALE_H() {
        return (int)1L;
    }
    public static int _FEATURES_H() {
        return (int)1L;
    }
    public static int _DEFAULT_SOURCE() {
        return (int)1L;
    }
    public static int __GLIBC_USE_ISOC2X() {
        return (int)0L;
    }
    public static int __USE_ISOC11() {
        return (int)1L;
    }
    public static int __USE_ISOC99() {
        return (int)1L;
    }
    public static int __USE_ISOC95() {
        return (int)1L;
    }
    public static int __USE_POSIX_IMPLICITLY() {
        return (int)1L;
    }
    public static int _POSIX_SOURCE() {
        return (int)1L;
    }
    public static int __USE_POSIX() {
        return (int)1L;
    }
    public static int __USE_POSIX2() {
        return (int)1L;
    }
    public static int __USE_POSIX199309() {
        return (int)1L;
    }
    public static int __USE_POSIX199506() {
        return (int)1L;
    }
    public static int __USE_XOPEN2K() {
        return (int)1L;
    }
    public static int __USE_XOPEN2K8() {
        return (int)1L;
    }
    public static int _ATFILE_SOURCE() {
        return (int)1L;
    }
    public static int __WORDSIZE() {
        return (int)64L;
    }
    public static int __WORDSIZE_TIME64_COMPAT32() {
        return (int)1L;
    }
    public static int __SYSCALL_WORDSIZE() {
        return (int)64L;
    }
    public static int __USE_MISC() {
        return (int)1L;
    }
    public static int __USE_ATFILE() {
        return (int)1L;
    }
    public static int __USE_FORTIFY_LEVEL() {
        return (int)0L;
    }
    public static int __GLIBC_USE_DEPRECATED_GETS() {
        return (int)0L;
    }
    public static int __GLIBC_USE_DEPRECATED_SCANF() {
        return (int)0L;
    }
    public static int _STDC_PREDEF_H() {
        return (int)1L;
    }
    public static int __STDC_IEC_559__() {
        return (int)1L;
    }
    public static int __STDC_IEC_559_COMPLEX__() {
        return (int)1L;
    }
    public static int __GNU_LIBRARY__() {
        return (int)6L;
    }
    public static int __GLIBC__() {
        return (int)2L;
    }
    public static int __GLIBC_MINOR__() {
        return (int)37L;
    }
    public static int _SYS_CDEFS_H() {
        return (int)1L;
    }
    public static int __glibc_c99_flexarr_available() {
        return (int)1L;
    }
    public static int __LDOUBLE_REDIRECTS_TO_FLOAT128_ABI() {
        return (int)0L;
    }
    public static int __HAVE_GENERIC_SELECTION() {
        return (int)1L;
    }
    public static int _BITS_LOCALE_H() {
        return (int)1L;
    }
    public static int __LC_CTYPE() {
        return (int)0L;
    }
    public static int __LC_NUMERIC() {
        return (int)1L;
    }
    public static int __LC_TIME() {
        return (int)2L;
    }
    public static int __LC_COLLATE() {
        return (int)3L;
    }
    public static int __LC_MONETARY() {
        return (int)4L;
    }
    public static int __LC_MESSAGES() {
        return (int)5L;
    }
    public static int __LC_ALL() {
        return (int)6L;
    }
    public static int __LC_PAPER() {
        return (int)7L;
    }
    public static int __LC_NAME() {
        return (int)8L;
    }
    public static int __LC_ADDRESS() {
        return (int)9L;
    }
    public static int __LC_TELEPHONE() {
        return (int)10L;
    }
    public static int __LC_MEASUREMENT() {
        return (int)11L;
    }
    public static int __LC_IDENTIFICATION() {
        return (int)12L;
    }
    public static int _BITS_TYPES_LOCALE_T_H() {
        return (int)1L;
    }
    public static int _BITS_TYPES___LOCALE_T_H() {
        return (int)1L;
    }
    public static MethodHandle setlocale$MH() {
        return RuntimeHelper.requireNonNull(constants$0.setlocale$MH,"setlocale");
    }
    public static MemoryAddress setlocale ( int __category,  Addressable __locale) {
        var mh$ = setlocale$MH();
        try {
            return (MemoryAddress)mh$.invokeExact(__category, __locale);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
    public static MethodHandle localeconv$MH() {
        return RuntimeHelper.requireNonNull(constants$0.localeconv$MH,"localeconv");
    }
    public static MemoryAddress localeconv () {
        var mh$ = localeconv$MH();
        try {
            return (MemoryAddress)mh$.invokeExact();
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
    public static OfAddress __locale_t = Constants$root.C_POINTER$LAYOUT;
    public static OfAddress locale_t = Constants$root.C_POINTER$LAYOUT;
    public static MethodHandle newlocale$MH() {
        return RuntimeHelper.requireNonNull(constants$0.newlocale$MH,"newlocale");
    }
    public static MemoryAddress newlocale ( int __category_mask,  Addressable __locale,  Addressable __base) {
        var mh$ = newlocale$MH();
        try {
            return (MemoryAddress)mh$.invokeExact(__category_mask, __locale, __base);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
    public static MethodHandle duplocale$MH() {
        return RuntimeHelper.requireNonNull(constants$0.duplocale$MH,"duplocale");
    }
    public static MemoryAddress duplocale ( Addressable __dataset) {
        var mh$ = duplocale$MH();
        try {
            return (MemoryAddress)mh$.invokeExact(__dataset);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
    public static MethodHandle freelocale$MH() {
        return RuntimeHelper.requireNonNull(constants$0.freelocale$MH,"freelocale");
    }
    public static void freelocale ( Addressable __dataset) {
        var mh$ = freelocale$MH();
        try {
            mh$.invokeExact(__dataset);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
    public static MethodHandle uselocale$MH() {
        return RuntimeHelper.requireNonNull(constants$0.uselocale$MH,"uselocale");
    }
    public static MemoryAddress uselocale ( Addressable __dataset) {
        var mh$ = uselocale$MH();
        try {
            return (MemoryAddress)mh$.invokeExact(__dataset);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
    public static long _POSIX_C_SOURCE() {
        return 200809L;
    }
    public static int __TIMESIZE() {
        return (int)64L;
    }
    public static long __STDC_IEC_60559_BFP__() {
        return 201404L;
    }
    public static long __STDC_IEC_60559_COMPLEX__() {
        return 201404L;
    }
    public static long __STDC_ISO_10646__() {
        return 201706L;
    }
    public static MemoryAddress NULL() {
        return constants$1.NULL$ADDR;
    }
    public static int LC_CTYPE() {
        return (int)0L;
    }
    public static int LC_NUMERIC() {
        return (int)1L;
    }
    public static int LC_TIME() {
        return (int)2L;
    }
    public static int LC_COLLATE() {
        return (int)3L;
    }
    public static int LC_MONETARY() {
        return (int)4L;
    }
    public static int LC_MESSAGES() {
        return (int)5L;
    }
    public static int LC_ALL() {
        return (int)6L;
    }
    public static int LC_PAPER() {
        return (int)7L;
    }
    public static int LC_NAME() {
        return (int)8L;
    }
    public static int LC_ADDRESS() {
        return (int)9L;
    }
    public static int LC_TELEPHONE() {
        return (int)10L;
    }
    public static int LC_MEASUREMENT() {
        return (int)11L;
    }
    public static int LC_IDENTIFICATION() {
        return (int)12L;
    }
    public static int LC_CTYPE_MASK() {
        return (int)1L;
    }
    public static int LC_NUMERIC_MASK() {
        return (int)2L;
    }
    public static int LC_TIME_MASK() {
        return (int)4L;
    }
    public static int LC_COLLATE_MASK() {
        return (int)8L;
    }
    public static int LC_MONETARY_MASK() {
        return (int)16L;
    }
    public static int LC_MESSAGES_MASK() {
        return (int)32L;
    }
    public static int LC_PAPER_MASK() {
        return (int)128L;
    }
    public static int LC_NAME_MASK() {
        return (int)256L;
    }
    public static int LC_ADDRESS_MASK() {
        return (int)512L;
    }
    public static int LC_TELEPHONE_MASK() {
        return (int)1024L;
    }
    public static int LC_MEASUREMENT_MASK() {
        return (int)2048L;
    }
    public static int LC_IDENTIFICATION_MASK() {
        return (int)4096L;
    }
    public static int LC_ALL_MASK() {
        return (int)8127L;
    }
    public static MemoryAddress LC_GLOBAL_LOCALE() {
        return constants$1.LC_GLOBAL_LOCALE$ADDR;
    }
}


