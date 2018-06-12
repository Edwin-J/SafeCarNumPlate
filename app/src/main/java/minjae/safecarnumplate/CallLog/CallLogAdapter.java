package minjae.safecarnumplate.CallLog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import minjae.safecarnumplate.R;

/**
 * Created by Minjae on 2018-06-07.
 */

public class CallLogAdapter extends BaseAdapter {

    private ArrayList<CallLogItem> items = new ArrayList<CallLogItem>();

    public CallLogAdapter() {

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_call_log, parent, false);
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.list_img);
        TextView num = (TextView) convertView.findViewById(R.id.list_num);
        TextView time = (TextView) convertView.findViewById(R.id.list_time);

        CallLogItem callLogItem = items.get(position);

        icon.setImageDrawable(callLogItem.getIcon());
        num.setText(callLogItem.getNumber());
        time.setText(callLogItem.getTime());

        return convertView;
    }

    public void addLog(Drawable icon, String num, String time) {
        CallLogItem item = new CallLogItem();

        item.setIcon(icon);
        item.setNumber(num);
        item.setTime(time);

        items.add(item);
    }

    public void delLog(int pos) {
        items.remove(pos);
    }

    public void clear() {
        items.clear();
    }

}
