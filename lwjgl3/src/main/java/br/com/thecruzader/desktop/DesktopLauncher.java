package br.com.thecruzader.desktop;

import br.com.thecruzader.CrusaderGame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;

public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static void main(String[] args) {
        ApplicationListener app;
        if (args.length == 0) app = new CrusaderGame();
        else if (args.length == 2 && args[0].equals("--smoke")) app = new SmokeTest(args[1]);
        else
            throw new IllegalArgumentException(
                    "Usage: The Cruzader [--smoke screenshot-directory]");
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("The Cruzader");
        config.setWindowedMode(1152, 648);
        config.setWindowSizeLimits(800, 450, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(120);
        config.setWindowListener(
                new Lwjgl3WindowAdapter() {
                    @Override
                    public void focusLost() {
                        app.pause();
                    }
                });
        new Lwjgl3Application(app, config);
    }
}
