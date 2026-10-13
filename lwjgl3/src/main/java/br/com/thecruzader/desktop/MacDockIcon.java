package br.com.thecruzader.desktop;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.macosx.ObjCRuntime;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.macosx.ObjCRuntime.*;

/** GLFW ignores window icons on macOS; set NSApplication's Dock image on its main thread. */
final class MacDockIcon {
    private static final long SEND = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");

    private MacDockIcon() {}

    static void apply() {
        byte[] png;
        try (var input = MacDockIcon.class.getResourceAsStream("/branding/icon-1024.png")) {
            if (input == null) throw new IllegalStateException("Missing Dock icon");
            png = input.readAllBytes();
        } catch (IOException error) {
            throw new UncheckedIOException("Cannot load Dock icon", error);
        }
        ByteBuffer bytes = MemoryUtil.memAlloc(png.length);
        long data = 0;
        long image = 0;
        try {
            bytes.put(png).flip();
            data = invokePPPPP(message(objc_getClass("NSData"), "alloc"),
                    sel_registerName("initWithBytes:length:"), MemoryUtil.memAddress(bytes),
                    (long) png.length, SEND);
            if (data == 0) throw new IllegalStateException("Cannot create Dock image data");
            image = invokePPPP(message(objc_getClass("NSImage"), "alloc"),
                    sel_registerName("initWithData:"), data, SEND);
            if (image == 0) throw new IllegalStateException("Cannot decode Dock image");
            long app = message(objc_getClass("NSApplication"), "sharedApplication");
            invokePPPV(app, sel_registerName("setApplicationIconImage:"), image, SEND);
        } finally {
            if (image != 0) invokePPV(image, sel_registerName("release"), SEND);
            if (data != 0) invokePPV(data, sel_registerName("release"), SEND);
            MemoryUtil.memFree(bytes);
        }
    }

    private static long message(long receiver, String selector) {
        return invokePPP(receiver, sel_registerName(selector), SEND);
    }
}
