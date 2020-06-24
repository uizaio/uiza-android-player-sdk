package com.uiza.sdk.dialog.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.uiza.sdk.R;
import com.uiza.sdk.widget.UZSwitch;

import java.util.List;

public class SettingAdapter extends ArrayAdapter<SettingItem> {

    private Context mContext;
    private List<SettingItem> items;

    public SettingAdapter(@NonNull Context context, List<SettingItem> list) {
        super(context, 0, list);
        mContext = context;
        items = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;
        final ViewHolder holder;
        SettingItem item = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (getItemViewType(position) == 0) {
                view = inflater.inflate(R.layout.uz_setting_list_item_toggle, null);
                holder = new ViewHolder0(view);
            } else {
                view = inflater.inflate(R.layout.uz_setting_list_item, null);
                holder = new ViewHolder(view);
            }
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        if (item != null) {
            holder.setTitle(item.getTitle());
            if (holder instanceof ViewHolder0) {
                ((ViewHolder0) holder).setChecked(item.isChecked());
                ((ViewHolder0) holder).setOnToggleChangeListener(item.getListener());
            }
        }
        return view;
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isToggle() ? 0 : 1;
    }

    static class ViewHolder0 extends ViewHolder {
        protected UZSwitch toggle;
        OnToggleChangeListener listener;

        public ViewHolder0(View root) {
            super(root);
            toggle = root.findViewById(R.id.toggle_box);
            root.setOnClickListener( v -> {
                if (listener != null) {
                    boolean nextCheck = !toggle.isChecked();
                    if (listener.onCheckedChanged(nextCheck))
                        toggle.setChecked(nextCheck);
                }
            });
            toggle.setOnCheckedChangeListener((bt, checked) -> {
                if (listener != null) {
                    if (listener.onCheckedChanged(checked))
                        toggle.setChecked(checked);
                }
            });
        }

        public void setChecked(boolean checked) {
            toggle.setChecked(checked);
        }

        public void setOnToggleChangeListener(OnToggleChangeListener listener) {
            this.listener = listener;
        }

        ;

    }

    static class ViewHolder {
        protected TextView text;

        public ViewHolder(View root) {
            text = root.findViewById(android.R.id.text1);
        }

        public void setTitle(String title) {
            this.text.setText(title);
        }
    }

    public interface OnToggleChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param isChecked The new checked state of buttonView.
         * @return is checked
         */
        boolean onCheckedChanged(boolean isChecked);
    }
}
