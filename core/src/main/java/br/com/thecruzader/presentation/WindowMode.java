package br.com.thecruzader.presentation;

/**
 * Platform-owned fullscreen behavior, called from the application's render thread.
 */
public interface WindowMode {
    boolean fullscreen();

    void toggleFullscreen();
}
