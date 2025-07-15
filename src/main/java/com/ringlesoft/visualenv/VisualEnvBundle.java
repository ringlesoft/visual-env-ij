package com.ringlesoft.visualenv;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

/**
 * Message bundle for Visual Env plugin.
 */
public final class VisualEnvBundle extends DynamicBundle {

    @NonNls
    private static final String BUNDLE = "messages.MyBundle";
    private static final VisualEnvBundle INSTANCE = new VisualEnvBundle();

    private VisualEnvBundle() {
        super(BUNDLE);
    }

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static @NotNull Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getLazyMessage(key, params);
    }
}
