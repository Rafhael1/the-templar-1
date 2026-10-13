package br.com.thecruzader.desktop;

import br.com.thecruzader.CrusaderGame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import org.lwjgl.system.Platform;

public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static void main(String[] args) {
        ApplicationListener app;
        if (args.length == 0) app = new CrusaderGame();
        else if (args.length == 2 && args[0].equals("--smoke")) app = new SmokeTest(args[1]);
        else if (args.length == 1 && args[0].equals("--profile")) app = new PerformanceTest();
        else
            throw new IllegalArgumentException(
                    "Usage: O Templário [--smoke screenshot-directory | --profile]");
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(CrusaderGame.TITLE);
        config.setWindowIcon("branding/icon-128.png", "branding/icon-64.png",
                "branding/icon-32.png", "branding/icon-16.png");
        config.setWindowedMode(960, 540);
        config.setWindowSizeLimits(640, 360, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(0);
        if (app instanceof PerformanceTest) {
            config.setWindowedMode(960, 540);
            config.useVsync(false);
            config.setForegroundFPS(0);
        }
        config.setWindowListener(
                new Lwjgl3WindowAdapter() {
                    @Override
                    public void created(Lwjgl3Window window) {
                        if (Platform.get() == Platform.MACOSX) MacDockIcon.apply();
                    }

                    @Override
                    public void focusLost() {
                        app.pause();
                    }
                });
        new Lwjgl3Application(app, config);
    }
}
