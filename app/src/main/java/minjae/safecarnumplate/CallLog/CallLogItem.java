package minjae.safecarnumplate.CallLog;

import android.graphics.drawable.Drawable;

/**
 * Created by Minjae on 2018-06-07.
 */

public class CallLogItem {
    private Drawable icon;
    private String number;
    private String time;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
