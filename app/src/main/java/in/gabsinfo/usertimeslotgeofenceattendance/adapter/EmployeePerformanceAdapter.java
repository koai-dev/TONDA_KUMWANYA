package in.gabsinfo.usertimeslotgeofenceattendance.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import in.gabsinfo.usertimeslotgeofenceattendance.Interface.IRecyclerViewItemClickListener;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PerformanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;

public class EmployeePerformanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int EMPTY_VIEW = 77777;
    private final Activity mActivity;
    private final String mWorkType;
    private final IRecyclerViewItemClickListener mIRecyclerViewItemClickListener;
    private ArrayList<PerformanceModel> mList = new ArrayList<>();
    private SimpleDateFormat minutusFormat=new SimpleDateFormat("mm", Locale.US);

    public EmployeePerformanceAdapter(Activity activity, IRecyclerViewItemClickListener iRecyclerViewItemClickListener,String workType,
                                      ArrayList<PerformanceModel> list) {
        mActivity = activity;
        mIRecyclerViewItemClickListener=iRecyclerViewItemClickListener;
        mWorkType=workType;
        mList = list;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        if (viewType == EMPTY_VIEW) {
            return new EmptyViewHolder(layoutInflater.inflate(R.layout.nothing_yet, parent, false));
        } else {
            return new MyViewHolder(layoutInflater.inflate(R.layout.row_employee_performance, parent, false));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == EMPTY_VIEW) {
            final EmptyViewHolder itemView = (EmptyViewHolder) holder;
            itemView.tvAlertMessage.setText(mActivity.getString(R.string.label_not_attendance_found));
        } else {
            final MyViewHolder itemView = (MyViewHolder) holder;
            final PerformanceModel performanceModel = mList.get(position);

            itemView.tvMonthYear.setText(performanceModel.getDate());
            itemView.tvPunchOutPending.setText(String.valueOf(performanceModel.getOutPending()!=0?performanceModel.getOutPending():"-"));
            itemView.tvFullDays.setText(String.valueOf(performanceModel.getFullDay()!=0?performanceModel.getFullDay():"-"));

            if(mWorkType.equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)){
                itemView.tvHalfDays.setVisibility(View.GONE);
                itemView.tvPresentButLeave.setVisibility(View.GONE);
                itemView.tvWorkingHours.setVisibility(View.VISIBLE);

                if(performanceModel.getTotalMinutes()!=0) {
                    Date mmDate = null;
                    try {
                        mmDate = minutusFormat.parse(String.valueOf(performanceModel.getTotalMinutes()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (mmDate != null) {
                        String[] workingHours = CommonMethods.actualTimeFormat.format(mmDate).split(":");
                        itemView.tvWorkingHours.setText(String.format(mActivity.getString(R.string.label_hours_minutes), workingHours[0], workingHours[1]));
                    }
                }else{
                    itemView.tvWorkingHours.setText("-");
                }
            }else{
                itemView.tvHalfDays.setVisibility(View.VISIBLE);
                itemView.tvPresentButLeave.setVisibility(View.VISIBLE);
                itemView.tvWorkingHours.setVisibility(View.GONE);

                itemView.tvHalfDays.setText(String.valueOf(performanceModel.getHalfDay()!=0?performanceModel.getHalfDay():"-"));
                itemView.tvPresentButLeave.setText(String.valueOf(performanceModel.getPresentButLeave()!=0?performanceModel.getPresentButLeave():"-"));
            }

            itemView.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIRecyclerViewItemClickListener.onRecyclerViewItemClick(performanceModel);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() > 0 ? mList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }


    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAlertMessage;

        public EmptyViewHolder(View view) {
            super(view);
            tvAlertMessage = (TextView) view.findViewById(R.id.tvAlertMessage);
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout root;
        TextView tvMonthYear;
        TextView tvPunchOutPending;
        TextView tvFullDays;
        TextView tvHalfDays;
        TextView tvPresentButLeave;
        TextView tvWorkingHours;

        public MyViewHolder(View view) {
            super(view);
            root =  itemView.findViewById(R.id.root);
            tvMonthYear =  itemView.findViewById(R.id.tv_month_year);
            tvPunchOutPending =  itemView.findViewById(R.id.tv_out_pending);
            tvFullDays =  itemView.findViewById(R.id.tv_full_days);
            tvHalfDays =  itemView.findViewById(R.id.tv_half_days);
            tvPresentButLeave =  itemView.findViewById(R.id.tv_present_but_leave);
            tvWorkingHours =  itemView.findViewById(R.id.tv_working_hours);
        }
    }

    public void addItem(PerformanceModel model) {
        mList.add(model);
        notifyItemInserted(mList.size());
    }

    public void clearAdapter(){
        if(mList!=null) {
            mList.clear();
            notifyDataSetChanged();
        }
    }

}
