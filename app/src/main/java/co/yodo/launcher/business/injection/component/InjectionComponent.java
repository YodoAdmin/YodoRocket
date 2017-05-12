package co.yodo.launcher.business.injection.component;

import co.yodo.launcher.business.injection.module.GuiModule;
import co.yodo.launcher.business.injection.scope.ApplicationScope;
import co.yodo.launcher.ui.RocketActivity;
import co.yodo.launcher.ui.MainActivity;
import co.yodo.launcher.ui.RegistrationActivity;
import co.yodo.launcher.ui.option.contract.IRequestOption;
import dagger.Component;

@ApplicationScope
@Component(modules = {GuiModule.class}, dependencies = ApplicationComponent.class)
public interface InjectionComponent {
    // Injects to the Activities
    void inject(MainActivity activity);
    void inject(RegistrationActivity activity);
    void inject(RocketActivity activity);

    // Injects to the Components
    void inject(IRequestOption option);
}
