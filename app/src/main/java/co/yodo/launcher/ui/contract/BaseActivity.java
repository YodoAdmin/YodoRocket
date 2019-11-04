package co.yodo.launcher.ui.contract;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import co.yodo.launcher.utils.GuiUtils;

/**
 * Created by hei on 22/04/17.
 * Base activity for the YodoRocket application
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GuiUtils.setLanguage(this);
    }

    /**
     * Sets the main UI controllers
     */
    protected abstract void setupGUI();

    /**
     * Updates the data in the activity
     */
    protected abstract void updateData();

    /**
     * Handles any update that the UI requires from an
     * external component
     */
    public void updateUI() {}
}
