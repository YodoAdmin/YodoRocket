package co.yodo.launcher;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import co.yodo.launcher.injection.component.ApplicationComponent;
import co.yodo.launcher.injection.component.DaggerApplicationComponent;
import co.yodo.launcher.injection.component.DaggerInjectionComponent;
import co.yodo.launcher.injection.component.InjectionComponent;
import co.yodo.launcher.injection.module.ApplicationModule;
import co.yodo.restapi.helper.AppConfig;
import co.yodo.restapi.network.ApiClient;

@ReportsCrashes(formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = HttpSender.Method.POST,
                reportType = HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text
)
public class YodoApplication extends Application {
    /** Component that build the dependencies */
    private static InjectionComponent mComponent;

    @Override
    protected void attachBaseContext( Context base ) {
        super.attachBaseContext( base );
        ACRA.init( this );

        // Sets the log flag and IP for the restapi
        ApiClient.IP = ApiClient.DEMO_IP;
        AppConfig.DEBUG = co.yodo.launcher.helper.AppConfig.DEBUG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
                .applicationModule( new ApplicationModule( this ) )
                .build();

        mComponent = DaggerInjectionComponent.builder()
                .applicationComponent( appComponent )
                .build();
    }

    public static InjectionComponent getComponent() {
        return mComponent;
    }
}
