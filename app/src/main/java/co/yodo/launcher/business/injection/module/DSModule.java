package co.yodo.launcher.business.injection.module;

import co.yodo.launcher.business.injection.scope.ApplicationScope;
import dagger.Module;
import dagger.Provides;
import sunmi.ds.DSKernel;

@Module
public class DSModule {
    @Provides
    @ApplicationScope
    public DSKernel providesDSKernel() {
        return DSKernel.newInstance();
    }
}
