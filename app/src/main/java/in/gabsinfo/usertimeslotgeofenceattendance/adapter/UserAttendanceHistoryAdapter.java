package in.gabsinfo.usertimeslotgeofenceattendance.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;

public class UserAttendanceHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_HEADER = 77778;
    private final int EMPTY_VIEW = 77777;
    private final Activity mActivity;
    private final String mWorkType;
    private final SimpleDateFormat actualDateFormat;
    private final SimpleDateFormat requiredDateFormat;
    List<AttendanceModel> mList = new ArrayList<>();
    private final OnItemClickListener listener;


    public interface OnItemClickListener {
        void onClick(AttendanceModel model, int position);
    }

    public UserAttendanceHistoryAdapter(Activity mActivity, String workType,
                                        ArrayList<AttendanceModel> mList,
                                        OnItemClickListener listener) {

        Collections.sort(mList, new Comparator< AttendanceModel >() {
            @Override public int compare(AttendanceModel p1, AttendanceModel p2) {
                return p1.getDay()- p2.getDay(); // Ascending
            }
        });

        this.mActivity = mActivity;
        mWorkType=workType;
        this.mList = mList;
        this.listener=listener;

        actualDateFormat=new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US);
        requiredDateFormat=new SimpleDateFormat(ConstantData.DAY_FORMAT, Locale.US);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == EMPTY_VIEW) {
            return new EmptyViewHolder(layoutInflater.inflate(R.layout.nothing_yet, parent, false));
        } if (viewType == TYPE_HEADER) {
            int layout = R.layout.row_header_for_day_wise;
            if (mWorkType.equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                layout = R.layout.row_header_for_hour_wise;
            }

            return new EmptyViewHolder(layoutInflater.inflate(layout, parent, false));
        }  else {
            return new MyViewHolder(layoutInflater.inflate(R.layout.row_user_attendance_history, parent, false));
        }
    }

    private AttendanceModel getItem(int position) {
        return mList.get(position - 1);
    }


    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == EMPTY_VIEW) {
            final EmptyViewHolder itemView = (EmptyViewHolder) holder;
            itemView.tvAlertMessage.setText(mActivity.getString(R.string.label_not_attendance_found));
        } else if (getItemViewType(position) == TYPE_HEADER) {
        } else {
            final MyViewHolder itemView = (MyViewHolder) holder;
            // Bind data to itemView

            final AttendanceModel model = getItem(position);
            //final AttendanceModel model = mList.get(position);

            if (mWorkType.equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)){
                bindData(itemView,model,position);
            }else{
                if (model.getType().equalsIgnoreCase(mActivity.getString(R.string.label_public_holiday))
                        || model.getType().equalsIgnoreCase(mActivity.getString(R.string.label_non_working_day))
                        || model.getType().equalsIgnoreCase(mActivity.getString(R.string.label_absent_day))) {
                    Date date=null;
                    try {
                        date = actualDateFormat.parse(model.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(date!=null){
                        itemView.tv_date.setText(requiredDateFormat.format(date));
                    }

                    itemView.tv_punch_in_time.setText("-");
                    itemView.tv_punch_out_time.setText("-");
                    itemView.tv_total_days.setText(String.valueOf(0));

                    if (model.getType().equalsIgnoreCase(mActivity.getString(R.string.label_public_holiday))) {
                        itemView.tv_type.setText(mActivity.getString(R.string.label_public_holiday));
                        itemView.tv_date.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPublicHoliday));
                    } else if (model.getType().equalsIgnoreCase(mActivity.getString(R.string.label_non_working_day))) {
                        itemView.tv_type.setText(mActivity.getString(R.string.label_non_working_day));
                        itemView.tv_date.setTextColor(ContextCompat.getColor(mActivity, R.color.colorNonWorkingDay));
                    } else if (model.getType().equalsIgnoreCase(mActivity.getString(R.string.label_absent_day))){
                        itemView.tv_type.setText(mActivity.getString(R.string.label_absent_day));
                        itemView.tv_date.setTextColor(ContextCompat.getColor(mActivity, R.color.colorAbsentDay));
                    }
                }else{
                    bindData(itemView,model,position);
                }
            }

        }
    }

    private void bindData(MyViewHolder itemView, AttendanceModel model, int position) {
        Date punchDate=null;
        try {
            punchDate = actualDateFormat.parse(model.getPunchDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(punchDate!=null){
            itemView.tv_date.setText(requiredDateFormat.format(punchDate));
        }
        if(model.getPresentDay()==-1){//Means user has not do punch out
            itemView.tv_punch_in_time.setText(model.getPunchInTime());
            itemView.tv_punch_out_time.setText("-");
            itemView.tv_total_days.setText(String.valueOf(0));
        }else{
            itemView.tv_punch_in_time.setText(model.getPunchInTime());
            itemView.tv_punch_out_time.setText(model.getPunchOutTime());

            if (mWorkType.equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)){
                String[] workingHours = model.getTotalWorkingHours().split(":");
                itemView.tv_total_days.setText(String.format(mActivity.getString(R.string.label_hours_minutes), workingHours[0], workingHours[1]));
            }else{
                if (model.getPresentDay() % 1 == 0)  // true: it's an integer, false: it's not an integer
                    itemView.tv_total_days.setText(String.valueOf((int) model.getPresentDay()));
                else
                    itemView.tv_total_days.setText(String.valueOf(model.getPresentDay()));
            }
        }

        if(model.getPresentDay()==-1){
            itemView.tv_type.setText(mActivity.getString(R.string.label_out_pending));
            itemView.tv_date.setTextColor(ContextCompat.getColor(mActivity,R.color.colorMissPunch));
        }else if (model.getPresentDay() == 1) {
            itemView.tv_type.setText(mActivity.getString(R.string.label_full_days));
            itemView.tv_date.setTextColor(ContextCompat.getColor(mActivity,R.color.colorFullDayPresent));
        } else if (!mWorkType.equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
            if (model.getPresentDay() == 0) {
                itemView.tv_type.setText(mActivity.getString(R.string.label_present_but_leave));
                itemView.tv_date.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPresentButLeaveBefore));
            } else if (model.getPresentDay() == 0.5) {
                itemView.tv_type.setText(mActivity.getString(R.string.label_half_days));
                itemView.tv_date.setTextColor(ContextCompat.getColor(mActivity, R.color.colorHalfDayPresent));
            }
        }

        itemView.ll_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(model, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size() > 0 ? mList.size()+1 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.size() == 0) {
            return EMPTY_VIEW;
        }else if (isPositionHeader(position))
            return TYPE_HEADER;
        return super.getItemViewType(position);
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAlertMessage;

        public EmptyViewHolder(View view) {
            super(view);
            tvAlertMessage =  view.findViewById(R.id.tvAlertMessage);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAlertMessage;

        public HeaderViewHolder(View view) {
            super(view);
            tvAlertMessage =  view.findViewById(R.id.tvAlertMessage);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout ll_root;
        TextView tv_date;
        TextView tv_total_days;
        TextView tv_punch_in_time;
        TextView tv_punch_out_time;
        TextView tv_type;

        public MyViewHolder(View view) {
            super(view);
            ll_root =  itemView.findViewById(R.id.ll_root);
            tv_date =  itemView.findViewById(R.id.tv_date);
            tv_total_days =  itemView.findViewById(R.id.tv_total_days);
            tv_punch_in_time =  itemView.findViewById(R.id.tv_punch_in_time);
            tv_punch_out_time =  itemView.findViewById(R.id.tv_punch_out_time);
            tv_type =  itemView.findViewById(R.id.tv_type);
        }
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void addData(ArrayList<AttendanceModel> list) {
        if(mList!=null)
            mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }
}