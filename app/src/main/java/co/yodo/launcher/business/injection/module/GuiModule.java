package co.yodo.launcher.business.injection.module;

import co.yodo.launcher.business.injection.scope.ApplicationScope;
import co.yodo.launcher.helper.ProgressDialogHelper;
import dagger.Module;
import dagger.Provides;

@Module
public class GuiModule {
    @Provides
    @ApplicationScope
    public ProgressDialogHelper providesProgressDialogHelper() {
        return new ProgressDialogHelper();
    }
}
