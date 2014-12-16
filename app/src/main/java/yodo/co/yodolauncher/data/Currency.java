package yodo.co.yodolauncher.data;

import android.graphics.drawable.Drawable;

/**
 * Created by luis on 16/12/14.
 */
public class Currency {
    private String   name;
    private Drawable img;

    public Currency(String name, Drawable img) {
        this.name = name;
        this.img = img;
    }

    public String getName() {
        return this.name;
    }

    public Drawable getImg() {
        return this.img;
    }
}
