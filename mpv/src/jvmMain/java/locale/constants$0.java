// Generated by jextract

package locale;

import java.lang.foreign.FunctionDescriptor;
import java.lang.invoke.MethodHandle;

class constants$0 {

    static final FunctionDescriptor setlocale$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle setlocale$MH = RuntimeHelper.downcallHandle(
        "setlocale",
        constants$0.setlocale$FUNC
    );
    static final FunctionDescriptor localeconv$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT);
    static final MethodHandle localeconv$MH = RuntimeHelper.downcallHandle(
        "localeconv",
        constants$0.localeconv$FUNC
    );
    static final FunctionDescriptor newlocale$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_INT$LAYOUT,
        Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle newlocale$MH = RuntimeHelper.downcallHandle(
        "newlocale",
        constants$0.newlocale$FUNC
    );
    static final FunctionDescriptor duplocale$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle duplocale$MH = RuntimeHelper.downcallHandle(
        "duplocale",
        constants$0.duplocale$FUNC
    );
    static final FunctionDescriptor freelocale$FUNC = FunctionDescriptor.ofVoid(
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle freelocale$MH = RuntimeHelper.downcallHandle(
        "freelocale",
        constants$0.freelocale$FUNC
    );
    static final FunctionDescriptor uselocale$FUNC = FunctionDescriptor.of(Constants$root.C_POINTER$LAYOUT,
        Constants$root.C_POINTER$LAYOUT
    );
    static final MethodHandle uselocale$MH = RuntimeHelper.downcallHandle(
        "uselocale",
        constants$0.uselocale$FUNC
    );
}

