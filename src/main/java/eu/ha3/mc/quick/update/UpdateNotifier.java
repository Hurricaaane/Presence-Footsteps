package eu.ha3.mc.quick.update;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * The Update Notifier.
 *
 * @author Hurry
 *
 */
public class UpdateNotifier extends Thread {

    private transient final String location;
    private transient final Version currentVersion;

    private transient final Reporter reporter;

    private transient boolean checkRun;

    private boolean enabled = true;

    private int displayRemaining = 0;
    private int displayCount = 3;

    @Nullable
    private Version lastKnownVersion;

    public UpdateNotifier(String location, Version currentVersion, Reporter reporter) {
        this.location = location;
        this.currentVersion = currentVersion;
        this.reporter = reporter;
    }

    public int getRemainingNotifications() {
        return displayRemaining;
    }

    public void attempt() {
        if (checkRun) {
            return;
        }

        checkRun = true;

        if (enabled) {
            start();
        }
    }

    @Override
    public void run() {
        try {
            checkUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkUpdates() throws Exception {
        URL url = new URL(String.format(location, currentVersion.number, currentVersion.type));
        InputStream contents = url.openStream();

        Gson gson = new Gson();
        Versions version = gson.fromJson(new JsonReader(new InputStreamReader(contents)), Versions.class);

        Version newVersion = version.getLatest(currentVersion);

        if (newVersion.number > currentVersion.number) {
            if (lastKnownVersion == null) {
                lastKnownVersion = newVersion;
                displayRemaining = displayCount;
            }

            if (displayRemaining-- > 0) {
                Thread.sleep(10000);

                reporter.report(newVersion, currentVersion);
            }

            save();
        }
    }

    public void save() {

    }

    public void loadConfig(Path file) {
    }

    static class Versions {
        List<Version> versions = new ArrayList<>();

        Version getLatest(Version current) {
            Version newVersion = new Version().upgrade(current);

            versions.forEach(newVersion::upgrade);

            return newVersion;
        }
    }

    public static class Version {
        public int number;

        public String minecraft;

        public String type;

        Version() {}

        public Version(String minecraft, String type, int number) {
            this.minecraft = minecraft;
            this.type = type;
            this.number = number;
        }

        Version upgrade(Version other) {
            if (number > other.number) {
                number = other.number;

                if (other.minecraft != null) {
                    minecraft = other.minecraft;
                }

                if (other.type != null) {
                    type = other.type;
                }
            }

            return this;
        }
    }

    @FunctionalInterface
    public interface Reporter {
        void report(Version newVersion, Version currentVersion);
    }
}
