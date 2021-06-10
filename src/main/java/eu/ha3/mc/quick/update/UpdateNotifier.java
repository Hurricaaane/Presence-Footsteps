package eu.ha3.mc.quick.update;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;
import eu.ha3.presencefootsteps.config.JsonFile;

/**
 * The Update Notifier.
 *
 * @author Hurry (Huricaaaan) (v1)
 * @author Sollace (v2)
 *
 */
public class UpdateNotifier extends JsonFile {

    private static final Logger logger = LogManager.getLogger("UpdateNotifier");

    private transient final String location;
    private transient final Version currentVersion;

    private transient final Reporter reporter;

    private transient boolean checkRun;

    private boolean enabled = true;

    private int displayRemaining = 0;
    private int displayCount = 3;

    @Nullable
    private Version lastKnownVersion;

    public UpdateNotifier(Path file, String location, Version currentVersion, Reporter reporter) {
        super(file);

        this.location = location;
        this.currentVersion = currentVersion;
        this.reporter = reporter;
    }

    public int getRemainingNotifications() {
        return displayRemaining;
    }

    public void attempt() {
        if (checkRun || !enabled) {
            return;
        }

        checkRun = true;

        Thread updateThread = new Thread(this::run);
        updateThread.setDaemon(true);
        updateThread.setName("UpdateChecker");
        updateThread.start();
    }

    private void run() {
        try {
            Version newVersion = readVersions().getLatest(currentVersion);

            if (!newVersion.equals(lastKnownVersion)) {
                lastKnownVersion = newVersion;
                displayRemaining = displayCount;
            }

            if (displayRemaining > 0 && newVersion.number > currentVersion.number) {
                displayRemaining--;

                Thread.sleep(10000);
                reporter.report(newVersion, currentVersion);
            }

            save();
        } catch (Exception e) {
            logger.error("Error occured whilst checking for updates", e);
        }
    }

    private Versions readVersions() throws IOException {
        URL url = new URL(String.format(location, currentVersion.number, currentVersion.type));

        try (Reader reader = new InputStreamReader(url.openStream())) {
            return gson.fromJson(reader, Versions.class);
        }
    }

    static class Versions {
        List<Version> versions = new ArrayList<>();

        Version getLatest(Version current) {
            Version newOwnVersion = new Version().upgrade(current);
            Version newAnyVersion = new Version().upgrade(current);

            versions.forEach(v -> {
                if (newOwnVersion.compatible(v)) {
                    newOwnVersion.upgrade(v);
                }
                newAnyVersion.upgrade(v);
            });

            logger.info("Detected versions: Installed {}, Best {}, Available {}", current, newOwnVersion, newAnyVersion);

            if (!newOwnVersion.equals(current)) {
                return newOwnVersion;
            }

            return newAnyVersion;
        }
    }

    public static class Version {
        public int number;

        public String minecraft;

        public String type;

        public Version() {}

        public Version(String minecraft, String type, int number) {
            this.minecraft = minecraft;
            this.type = type;
            this.number = number;
        }

        Version upgrade(Version other) {
            if (number < other.number) {
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

        boolean compatible(Version other) {
            return (Strings.isNullOrEmpty(minecraft) || minecraft.equals(other.minecraft))
                && (Strings.isNullOrEmpty(type) || type.equals(other.type));
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other) ||
                    (other instanceof Version
                        && ((Version)other).number == number
                        && Strings.nullToEmpty(minecraft).equals(Strings.nullToEmpty(((Version)other).minecraft))
                        && Strings.nullToEmpty(type).equals(Strings.nullToEmpty(((Version)other).type)));
        }

        @Override
        public String toString() {
            return String.format("%s/%d/%s", type, number, minecraft);
        }
    }

    @FunctionalInterface
    public interface Reporter {
        void report(Version newVersion, Version currentVersion);
    }
}
