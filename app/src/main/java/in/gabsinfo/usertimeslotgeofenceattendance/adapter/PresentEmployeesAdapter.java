package in.gabsinfo.usertimeslotgeofenceattendance.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;

public class PresentEmployeesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Filterable {
    private final int EMPTY_VIEW = 77777;
    private final Activity mActivity;
    private ArrayList<PersonModel> mList;
    private List<PersonModel> mListFiltered;
    private final OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(AttendanceModel item);
    }


    public PresentEmployeesAdapter(Activity activity,
                                   ArrayList<PersonModel> list,
                                   OnItemClickListener listener) {
        mActivity = activity;
        mList = list;
        mListFiltered = mList;
        mListener = listener;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        if (viewType == EMPTY_VIEW) {
            return new EmptyViewHolder(layoutInflater.inflate(R.layout.nothing_yet, parent, false));
        } else {
            return new MyViewHolder(layoutInflater.inflate(R.layout.row_present_employee, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) != EMPTY_VIEW) {
            ((MyViewHolder) holder).bind(mListFiltered.get(position), position, mListener);


        } else {
            final EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.tvAlertMessage.setText(mActivity.getString(R.string.label_no_employee_added_for_this_branch));
        }
    }

    @Override
    public int getItemCount() {
        return mListFiltered.size() > 0 ? mListFiltered.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mListFiltered.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mListFiltered = mList;
                } else {
                    List<PersonModel> filteredList = new ArrayList<>();
                    for (PersonModel row : mList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    mListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mListFiltered = (ArrayList<PersonModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlertMessage;

        EmptyViewHolder(View view) {
            super(view);
            tvAlertMessage = view.findViewById(R.id.tvAlertMessage);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llRoot;
        ImageView ivUserProfileImage;
        ImageView ivPresentStatus;
        TextView tvPersonName;
        TextView tvPersonDesignation;
        TextView tvPunchInTime;
        TextView tvPunchOutTime;
        TextView tvCall;
        TextView tvViewInMap;

        public MyViewHolder(View view) {
            super(view);
            llRoot = itemView.findViewById(R.id.ll_root);
            ivUserProfileImage = itemView.findViewById(R.id.iv_user_profile_image);
            ivPresentStatus = itemView.findViewById(R.id.iv_present_status);
            tvPersonName = itemView.findViewById(R.id.tv_person_name);
            tvPersonDesignation = itemView.findViewById(R.id.tv_person_designation);
            tvPunchInTime = itemView.findViewById(R.id.tv_punch_in_time);
            tvPunchOutTime = itemView.findViewById(R.id.tv_punch_out_time);
            tvCall = itemView.findViewById(R.id.tv_call);
            tvViewInMap = itemView.findViewById(R.id.tv_view_in_map);
        }

        void bind(final PersonModel personModel, final int position, final OnItemClickListener listener) {
            if (personModel.getProfileImage() != null
                    && personModel.getProfileImage().trim().length() > 0) {
                CommonMethods.loadImage(mActivity, personModel.getProfileImage(), ivUserProfileImage,
                        ContextCompat.getDrawable(mActivity, R.drawable.img_module_user_profile));
            } else {
                CommonMethods.loadDefaultImage(mActivity, ivUserProfileImage,
                        ContextCompat.getDrawable(mActivity, R.drawable.img_module_user_profile));
            }


            tvPersonName.setText(personModel.getName());
            tvPersonDesignation.setText(personModel.getDesignation());

            if (personModel.getPunchInTime() != null
                    || personModel.getPunchOutTime() != null) {
                ivPresentStatus.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.bg_online));
            }else{
                ivPresentStatus.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.bg_offline));
            }

            if (personModel.getPunchInTime() != null) {
                tvPunchInTime.setText(personModel.getPunchInTime());
                tvPunchInTime.setTextColor(ContextCompat.getColor(mActivity, R.color.colorFullDayPresent));
            } else {
                tvPunchInTime.setText("N/A");
                tvPunchInTime.setTextColor(ContextCompat.getColor(mActivity, android.R.color.holo_red_dark));
            }

            if (personModel.getPunchOutTime() != null) {
                tvPunchOutTime.setText(personModel.getPunchOutTime());
                tvPunchOutTime.setTextColor(ContextCompat.getColor(mActivity, R.color.colorFullDayPresent));
            } else {
                tvPunchOutTime.setText("N/A");
                tvPunchOutTime.setTextColor(ContextCompat.getColor(mActivity, android.R.color.holo_red_dark));
            }

            if (personModel.getPunchInLatitude() != 0
                    || personModel.getPunchInLongitude() != 0
                    || personModel.getPunchOutLatitude() != 0
                    || personModel.getPunchOutLongitude() != 0) {
                tvViewInMap.setVisibility(View.VISIBLE);
            }else{
                tvViewInMap.setVisibility(View.GONE);
            }

            tvCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonMethods.call(mActivity,personModel.getMobileNo());
                }
            });

            tvViewInMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (personModel.getPunchInLatitude() != 0
                            || personModel.getPunchInLongitude() != 0
                            || personModel.getPunchOutLatitude() != 0
                            || personModel.getPunchOutLongitude() != 0) {
                        AttendanceModel attendanceModel = new AttendanceModel();
                        attendanceModel.setPunchDate(personModel.getPunchDate());
                        attendanceModel.setPunchInTime(personModel.getPunchInTime());
                        attendanceModel.setPunchInLatitude(personModel.getPunchInLatitude());
                        attendanceModel.setPunchInLongitude(personModel.getPunchInLongitude());
                        attendanceModel.setPunchOutTime(personModel.getPunchOutTime());
                        attendanceModel.setPunchOutLatitude(personModel.getPunchOutLatitude());
                        attendanceModel.setPunchOutLongitude(personModel.getPunchOutLongitude());
                        listener.onItemClick(attendanceModel);
                    }
                }
            });
        }
    }


    public void addData(ArrayList<PersonModel> list) {
        if (mList != null)
            mList.clear();
        else
            mList = new ArrayList<>();

        mList.addAll(list);
        mListFiltered=mList;
        notifyDataSetChanged();
    }

    public void clear() {
        if (mList != null)
            mList.clear();
        if (mListFiltered != null)
            mListFiltered.clear();
        notifyDataSetChanged();
    }
}

