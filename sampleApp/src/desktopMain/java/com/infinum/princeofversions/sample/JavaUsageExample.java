package com.infinum.princeofversions.sample;

import com.infinum.princeofversions.BaseUpdateResult;
import com.infinum.princeofversions.JvmPrinceOfVersionsKt;
import com.infinum.princeofversions.PrinceOfVersionsBase;
import com.infinum.princeofversions.UpdateStatus;
import com.infinum.princeofversions.java.JavaLoader;
import com.infinum.princeofversions.java.PrinceOfVersionsKt;
import com.infinum.princeofversions.java.UpdaterCallback;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class JavaUsageExample {

    private static final String UPDATE_URL = "https://pastebin.com/raw/SCyxsrK0";
    private static final long DELAY_TIME = 4000L;

    private final PrinceOfVersionsBase<String> princeOfVersions;
    private final Consumer<String> onStatusUpdate;
    private final Executor callbackExecutor;
    private Future<?> task = null;

    private final UpdaterCallback<String> defaultCallback = new UpdaterCallback<String>() {
        @Override
        public void onSuccess(@NotNull BaseUpdateResult<String> result) {
            handleUpdateResult(result);
        }

        @Override
        public void onError(@NotNull Throwable error) {
            error.printStackTrace();
            onStatusUpdate.accept("Error: " + error.getMessage());
        }
    };

    public JavaUsageExample(Consumer<String> onStatusUpdate) {
        this.onStatusUpdate = onStatusUpdate;
        this.callbackExecutor = Executors.newSingleThreadExecutor();
        this.princeOfVersions = JvmPrinceOfVersionsKt.PrinceOfVersions(getClass());
    }

    public void checkForUpdates() {
        checkForUpdates(false);
    }

    public void checkForUpdatesWithDelay() {
        checkForUpdates(true);
    }

    public void cancelUpdateCheck() {
        if (task != null && !task.isDone()) {
            task.cancel(true);
            onStatusUpdate.accept("Update check cancelled.");
        } else {
            onStatusUpdate.accept("Nothing to cancel.");
        }
    }

    private void checkForUpdates(boolean isSlow) {
        if (task != null) {
            task.cancel(true);
        }

        if (isSlow) {
            onStatusUpdate.accept("Starting delayed check (4s)...");
            JavaLoader slowLoader = createSlowLoader();
            task = PrinceOfVersionsKt.checkForUpdates(princeOfVersions, slowLoader, callbackExecutor, defaultCallback);
        } else {
            onStatusUpdate.accept("Checking for updates...");
            task = PrinceOfVersionsKt.checkForUpdates(princeOfVersions, UPDATE_URL, callbackExecutor, defaultCallback);
        }
    }

    private void handleUpdateResult(BaseUpdateResult<String> result) {
        String message;
        if (result.getStatus() == UpdateStatus.MANDATORY) {
            message = "A mandatory update to version " + result.getVersion() + " is available.";
        } else if (result.getStatus() == UpdateStatus.OPTIONAL) {
            message = "An optional update to version " + result.getVersion() + " is available.";
        } else {
            message = "You are on the latest version.";
        }
        onStatusUpdate.accept(message);
    }

    private JavaLoader createSlowLoader() {
        return new JavaLoader() {
            @NotNull
            @Override
            public String load() throws Throwable {
                Thread.sleep(DELAY_TIME);
                HttpURLConnection conn = (HttpURLConnection) new URL(UPDATE_URL).openConnection();

                InputStream response = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8));
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                reader.close();

                // Update status after delay completes
                onStatusUpdate.accept("Checking for updates...");

                return out.toString();
            }
        };
    }
}
