// Generated by jextract

package mpv;

import java.lang.foreign.FunctionDescriptor;
import java.lang.invoke.MethodHandle;

class constants$1 {

    static final FunctionDescriptor mpv_initialize$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle mpv_initialize$MH = RuntimeHelper.downcallHandle(
        "mpv_initialize",
        constants$1.mpv_initialize$FUNC
    );
    static final FunctionDescriptor mpv_destroy$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle mpv_destroy$MH = RuntimeHelper.downcallHandle(
        "mpv_destroy",
        constants$1.mpv_destroy$FUNC
    );
    static final FunctionDescriptor mpv_terminate_destroy$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle mpv_terminate_destroy$MH = RuntimeHelper.downcallHandle(
        "mpv_terminate_destroy",
        constants$1.mpv_terminate_destroy$FUNC
    );
    static final FunctionDescriptor mpv_create_client$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle mpv_create_client$MH = RuntimeHelper.downcallHandle(
        "mpv_create_client",
        constants$1.mpv_create_client$FUNC
    );
    static final FunctionDescriptor mpv_create_weak_client$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle mpv_create_weak_client$MH = RuntimeHelper.downcallHandle(
        "mpv_create_weak_client",
        constants$1.mpv_create_weak_client$FUNC
    );
    static final FunctionDescriptor mpv_load_config_file$FUNC = FunctionDescriptor.of(Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle mpv_load_config_file$MH = RuntimeHelper.downcallHandle(
        "mpv_load_config_file",
        constants$1.mpv_load_config_file$FUNC
    );
}


