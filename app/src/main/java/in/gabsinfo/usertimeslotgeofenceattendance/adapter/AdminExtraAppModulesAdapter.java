package in.gabsinfo.usertimeslotgeofenceattendance.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AppModuleModel;

public class AdminExtraAppModulesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity mActivity;
    private int mResource;
    List<AppModuleModel> mList;
    private final OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(AppModuleModel item, int Position);
    }


    public AdminExtraAppModulesAdapter(Activity activity, int resource, List<AppModuleModel> list,
                                       OnItemClickListener listener) {
        mActivity = activity;
        mResource = resource;
        mList = list;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new MyViewHolder(layoutInflater.inflate(mResource, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final AppModuleModel model = mList.get(position);
        ((MyViewHolder) holder).bind(model, position, mListener);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llHeader;
        ImageView ivModule;
        TextView tvModuleName;
        TextView tvModuleDescription;

        MyViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(false);

            llHeader = itemView.findViewById(R.id.ll_header);
            ivModule = itemView.findViewById(R.id.iv_module);
            tvModuleName = itemView.findViewById(R.id.tv_module_name);
            tvModuleDescription = itemView.findViewById(R.id.tv_module_description);
        }


        void bind(final AppModuleModel model, final int position, final OnItemClickListener listener) {

            llHeader.setBackgroundColor(Color.parseColor(model.getModuleColor()));
            tvModuleName.setText(model.getModuleName());
            tvModuleDescription.setText(model.getModuleDesc());
            if (model.getModuleId() == 31) {//Broadcast Notification
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.img_module_broadcast_message));
            } else if (model.getModuleId() == 32) {//Notes
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.img_module_notes));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(model, position);
                }
            });
        }
    }
}

