package co.yodo.launcher.injection.component;

import co.yodo.launcher.injection.module.ApiClientModule;
import co.yodo.launcher.injection.scope.ApplicationScope;
import co.yodo.launcher.ui.LauncherActivity;
import co.yodo.launcher.ui.MainActivity;
import co.yodo.launcher.ui.RegistrationActivity;
import co.yodo.launcher.ui.option.contract.IRequestOption;
import dagger.Component;

@ApplicationScope
@Component(
        modules = { ApiClientModule.class},
        dependencies = ApplicationComponent.class
)
public interface InjectionComponent {
    // Injects to the Activities
    void inject( MainActivity activity );
    void inject( RegistrationActivity activity );
    void inject( LauncherActivity activity );

    // Injects to the Components
    void inject( IRequestOption option );
}
