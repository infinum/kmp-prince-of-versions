package com.infinum.princeofversions.sample;

import android.os.Bundle;
import android.widget.Toast;

import com.infinum.princeofversions.BaseUpdateResult;
import com.infinum.princeofversions.PrinceOfVersionsBase;
import com.infinum.princeofversions.UpdateStatus;
import com.infinum.princeofversions.java.PrinceOfVersionsKt;
import com.infinum.princeofversions.java.JavaLoader;
import com.infinum.princeofversions.java.UpdaterCallback;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsControllerCompat;
import kotlin.Unit;

import static com.infinum.princeofversions.AndroidPrinceOfVersionsKt.PrinceOfVersions;
import static com.infinum.princeofversions.sample.ComposeJavaBridgeKt.setExampleScreenContent;

public class JavaUsageExample extends ComponentActivity {

    private final UpdaterCallback<Long> defaultCallback = new UpdaterCallback<Long>() {
        @Override
        public void onSuccess(@NonNull BaseUpdateResult<Long> result) {
            handleUpdateResult(result);
        }

        @Override
        public void onError(Throwable error) {
            error.printStackTrace();
            showToast(String.format(getString(R.string.update_exception), error.getMessage()));
        }
    };

    private static final String UPDATE_URL = "https://pastebin.com/raw/KPzkwNuP";
    private static final long DELAY_TIME = 3000L;

    private PrinceOfVersionsBase<Long> princeOfVersions;
    private Future<?> task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.activity.EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
            .setAppearanceLightStatusBars(true);

        princeOfVersions = PrinceOfVersions(this);

        setExampleScreenContent(
            this,
            () -> {
                checkForUpdates(false);
                return Unit.INSTANCE;
            },
            () -> {
                checkForUpdates(true);
                return Unit.INSTANCE;
            },
            () -> {
                cancelUpdateCheck();
                return Unit.INSTANCE;
            }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelUpdateCheck();
    }

    private void checkForUpdates(boolean isSlow) {
        if (task != null) {
            task.cancel(true);
        }

        if (isSlow) {
            JavaLoader slowLoader = createSlowLoader();
            task = PrinceOfVersionsKt.checkForUpdates(princeOfVersions, slowLoader, defaultCallback);
        } else {
            task = PrinceOfVersionsKt.checkForUpdates(princeOfVersions, UPDATE_URL, defaultCallback);
        }
    }

    private void cancelUpdateCheck() {
        if (task != null ) {
            task.cancel(true);
        }
    }

    private void handleUpdateResult(com.infinum.princeofversions.BaseUpdateResult<Long> result) {
        String message;
        if (result.getStatus() == UpdateStatus.MANDATORY) {
            message = getString(
                R.string.update_available_msg,
                getString(R.string.mandatory),
                result.getVersion()
            );
        } else if (result.getStatus() == UpdateStatus.OPTIONAL) {
            message = getString(
                R.string.update_available_msg,
                getString(R.string.not_mandatory),
                result.getVersion()
            );
        } else {
            message = getString(R.string.no_update_available);
        }
        showToast(message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private JavaLoader createSlowLoader() {
        return new JavaLoader() {
            @NonNull
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

                return out.toString();
            }
        };
    }
}
