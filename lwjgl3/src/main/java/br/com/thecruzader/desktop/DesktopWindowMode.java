package br.com.thecruzader.desktop;

import br.com.thecruzader.presentation.WindowMode;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import org.lwjgl.system.Platform;

/**
 * macOS uses its native fullscreen Space; other desktops use the graphics backend.
 */
final class DesktopWindowMode implements WindowMode {
    private long handle;
    private int windowedWidth, windowedHeight;

    void attach(Lwjgl3Window window) {
        handle = window.getWindowHandle();
        if (Platform.get() == Platform.MACOSX) MacFullscreen.prepare(handle);
    }

    @Override
    public boolean fullscreen() {
        if (handle == 0) throw new IllegalStateException("Window has not been created");
        return Platform.get() == Platform.MACOSX ? MacFullscreen.active(handle) : Gdx.graphics.isFullscreen();
    }

    @Override
    public void toggleFullscreen() {
        if (handle == 0) throw new IllegalStateException("Window has not been created");
        if (Platform.get() == Platform.MACOSX) {
            MacFullscreen.toggle(handle);
        } else if (Gdx.graphics.isFullscreen()) {
            if (!Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight))
                throw new IllegalStateException("Could not restore windowed mode");
        } else {
            windowedWidth = Gdx.graphics.getWidth();
            windowedHeight = Gdx.graphics.getHeight();
            if (!Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode()))
                throw new IllegalStateException("Could not enter fullscreen");
        }
    }
}
