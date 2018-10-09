package co.yodo.launcher;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import co.yodo.launcher.business.injection.component.ApplicationComponent;
import co.yodo.launcher.business.injection.component.DaggerApplicationComponent;
import co.yodo.launcher.business.injection.component.DaggerInjectionComponent;
import co.yodo.launcher.business.injection.component.InjectionComponent;
import co.yodo.launcher.business.injection.module.ApplicationModule;
import co.yodo.restapi.YodoApi;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class YodoApplication extends Application {
    /** Component that build the dependencies */
    private static InjectionComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        // Injection
        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        component = DaggerInjectionComponent.builder()
                .applicationComponent(appComponent)
                .build();

        // Init timber
        if (BuildConfig.DEBUG) {
            // Develop -- All logs on
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(@NonNull StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });
        } else {
            // Release -- Remove unimportant logs
            Timber.plant(new CrashReportingTree());
        }

        // Starts the Yodo API for requests
        YodoApi.init(this)
                .setLog(BuildConfig.DEBUG)
                .server(YodoApi.DEMO_IP, "E")
                .build();
    }

    public static InjectionComponent getComponent() {
        return component;
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        /** The max size of a line */
        private static final int MAX_LOG_LENGTH = 4000;
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            if (message.length() < MAX_LOG_LENGTH) {
                if (priority == Log.ASSERT) {
                    Log.wtf(tag, message);
                } else {
                    Log.println(priority, tag, message);
                }
                return;
            }

            for (int i = 0, length = message.length(); i < length; i++) {
                int newLine = message.indexOf('\n', i);
                newLine = newLine != -1 ? newLine : length;
                do {
                    int end = Math.min(newLine, i + MAX_LOG_LENGTH);
                    String part = message.substring(i, end);
                    if (priority == Log.ASSERT) {
                        Log.wtf(tag, part);
                    } else {
                        Log.println(priority, tag, part);
                    }
                    i = end;
                } while (i < newLine);
            }
        }
    }
}
