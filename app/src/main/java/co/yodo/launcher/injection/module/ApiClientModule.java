package co.yodo.launcher.injection.module;

import android.content.Context;

import co.yodo.launcher.injection.scope.ApplicationScope;
import co.yodo.launcher.ui.notification.ProgressDialogHelper;
import co.yodo.restapi.network.ApiClient;
import dagger.Module;
import dagger.Provides;

@Module
public class ApiClientModule {
    @Provides
    @ApplicationScope
    public ProgressDialogHelper providesProgressDialogHelper() {
        return new ProgressDialogHelper();
    }

    @Provides
    @ApplicationScope
    public ApiClient providesApiClient( Context context ) {
        return ApiClient.getInstance( context );
    }
}
