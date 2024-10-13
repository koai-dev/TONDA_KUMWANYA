package in.gabsinfo.usertimeslotgeofenceattendance.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.gabsinfo.usertimeslotgeofenceattendance.BuildConfig;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AppModuleModel;

public class AdminAppModulesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity mActivity;
    int mResource;
    List<AppModuleModel> mList;
    private final OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(AppModuleModel item, int Position);
    }



    public AdminAppModulesAdapter(Activity activity, int resource, List<AppModuleModel> list,
                                  OnItemClickListener listener) {
        mActivity = activity;
        mResource = resource;
        mList = list;
        mListener=listener;
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
        ((MyViewHolder) holder).bind(model, position,mListener);
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

        ImageView ivModule;
        TextView tvModuleName;
        TextView tvModuleDescription;
        ImageView ivNewFeatures;

        MyViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(false);

            ivModule =  itemView.findViewById(R.id.iv_module);
            tvModuleName =  itemView.findViewById(R.id.tv_module_name);
            tvModuleDescription =  itemView.findViewById(R.id.tv_module_description);
            ivNewFeatures =  itemView.findViewById(R.id.iv_new_features);
        }


        void bind(final AppModuleModel model, final int position,final OnItemClickListener listener) {

            if(model.getVersionCode()== BuildConfig.VERSION_CODE){
                ivNewFeatures.setVisibility(View.VISIBLE);
            }else{
                ivNewFeatures.setVisibility(View.GONE);
            }
            tvModuleName.setText(model.getModuleName());
            tvModuleDescription.setText(model.getModuleDesc());
            if (model.getModuleId()==2) {//Add Holidays
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.img_module_leave));
            } else if (model.getModuleId()==3) {//Add Person
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.img_module_add_person));
            } else if (model.getModuleId()==4) {//View Person
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.img_module_view_persons));
            }  else if (model.getModuleId()==5) {//Check Today Present Employees
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.img_module_today_presence));
            }  else if (model.getModuleId()==6) {//Calculate salary
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.img_module_cal_salary));
            }  else if (model.getModuleId()==7) {//Performance
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.img_module_performance));
            } else if (model.getModuleId()==8) {//Track employee current location
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.img_module_tracking));
            } else if (model.getModuleId()==9) {//Change password
                ivModule.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.img_module_change_password));
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
