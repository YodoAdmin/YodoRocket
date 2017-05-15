package co.yodo.launcher;

import android.app.Application;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import co.yodo.launcher.business.injection.component.ApplicationComponent;
import co.yodo.launcher.business.injection.component.DaggerApplicationComponent;
import co.yodo.launcher.business.injection.component.DaggerInjectionComponent;
import co.yodo.launcher.business.injection.component.InjectionComponent;
import co.yodo.launcher.business.injection.module.ApplicationModule;
import co.yodo.restapi.YodoApi;
import timber.log.Timber;

@ReportsCrashes(formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = HttpSender.Method.POST,
                reportType = HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.text_crash_toast
)
public class YodoApplication extends Application {
    /** Component that build the dependencies */
    private static InjectionComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

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
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });
        } else {
            // Release -- Remove unimportant logs
            Timber.plant(new CrashReportingTree());

            // Use acra
            ACRA.init(this);
        }

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
        protected void log(int priority, String tag, String message, Throwable t) {
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
