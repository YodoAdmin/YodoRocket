package co.yodo.launcher.ui.dialog.contract;

/**
 * Created by hei on 25/04/17.
 * Interface for the popups
 */
public interface IPopup {
    /**
     * Shows a loading bar in order to
     * load the data
     */
    void load();

    /**
     * Shows the data in the popup
     * @param value The value to setData in the TextView
     */
    void setData(String value);
}
