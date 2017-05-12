package co.yodo.launcher.business.injection.component;

import android.content.Context;

import javax.inject.Singleton;

import co.yodo.launcher.business.injection.module.ApplicationModule;
import dagger.Component;

@Singleton
@Component( modules = ApplicationModule.class )
public interface ApplicationComponent {
    /**
     * Exposes the Application Context to any component
     * which depends on this
     */
    Context providesApplication();
}