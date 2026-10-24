package br.com.thecruzader.desktop;

import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.system.macosx.ObjCRuntime;

import static org.lwjgl.system.JNI.invokePPP;
import static org.lwjgl.system.JNI.invokePPPV;
import static org.lwjgl.system.macosx.ObjCRuntime.sel_registerName;

/**
 * Uses AppKit fullscreen instead of a borderless or exclusive GLFW window.
 */
final class MacFullscreen {
    private static final long SEND = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");
    private static final long FULLSCREEN = 1L << 14;
    private static final long PRIMARY = 1L << 7, AUXILIARY = 1L << 8;

    private MacFullscreen() {
    }

    static void prepare(long handle) {
        long window = window(handle);
        long behavior = invokePPP(window, sel_registerName("collectionBehavior"), SEND);
        invokePPPV(window, sel_registerName("setCollectionBehavior:"), (behavior | PRIMARY) & ~AUXILIARY, SEND);
    }

    static boolean active(long handle) {
        return (invokePPP(window(handle), sel_registerName("styleMask"), SEND) & FULLSCREEN) != 0;
    }

    static void toggle(long handle) {
        invokePPPV(window(handle), sel_registerName("toggleFullScreen:"), 0L, SEND);
    }

    private static long window(long handle) {
        long window = GLFWNativeCocoa.glfwGetCocoaWindow(handle);
        if (window == 0) throw new IllegalStateException("Cannot access the native macOS window");
        return window;
    }
}
