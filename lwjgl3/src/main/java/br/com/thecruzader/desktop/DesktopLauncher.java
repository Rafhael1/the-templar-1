package br.com.thecruzader.desktop;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.GameVersion;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import org.lwjgl.system.Platform;

public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--version")) {
            System.out.println(CrusaderGame.TITLE + " " + GameVersion.CURRENT);
            return;
        }
        ApplicationListener app;
        DesktopWindowMode windowMode = new DesktopWindowMode();
        if (args.length == 0) app = new CrusaderGame(br.com.thecruzader.level.CampaignLoader.PATH, windowMode);
        else if (args.length == 2 && args[0].equals("--smoke")) app = new SmokeTest(args[1], windowMode);
        else if (args.length == 1 && args[0].equals("--profile")) app = new PerformanceTest(false);
        else if (args.length == 1 && args[0].equals("--profile-paced")) app = new PerformanceTest(true);
        else
            throw new IllegalArgumentException(
                "Usage: O Templário [--version | --smoke screenshot-directory | --profile | --profile-paced]");
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(CrusaderGame.TITLE);
        config.setWindowIcon("branding/icon-128.png", "branding/icon-64.png",
                "branding/icon-32.png", "branding/icon-16.png");
        config.setWindowedMode(960, 540);
        config.setWindowSizeLimits(640, 360, -1, -1);
        // The frame limit defines the cadence; vsync aligns presentation to the display.
        config.useVsync(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        if (args.length == 1 && args[0].equals("--profile")) {
            config.setWindowedMode(960, 540);
            config.useVsync(false);
            config.setForegroundFPS(0);
        }
        config.setWindowListener(
                new Lwjgl3WindowAdapter() {
                    @Override
                    public void created(Lwjgl3Window window) {
                        windowMode.attach(window);
                        if (Platform.get() == Platform.MACOSX) MacDockIcon.apply();
                    }

                    @Override
                    public void focusLost() {
                        app.pause();
                    }

                    @Override
                    public void focusGained() {
                        app.resume();
                    }
                });
        new Lwjgl3Application(app, config);
    }
}
