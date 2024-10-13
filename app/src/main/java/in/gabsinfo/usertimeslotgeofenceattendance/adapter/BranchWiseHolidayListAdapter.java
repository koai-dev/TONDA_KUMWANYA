package in.gabsinfo.usertimeslotgeofenceattendance.adapter;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.model.HolidayModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;

public class BranchWiseHolidayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder > {
    private Activity mActivity;
    private List<HolidayModel> mList;

    public BranchWiseHolidayListAdapter(Activity activity, List<HolidayModel> notesList) {
        mActivity = activity;
        mList = notesList;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        return new MyViewHolder(layoutInflater.inflate(R.layout.row_public_holiday_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder  holder, int position) {
        final MyViewHolder viewHolder = (MyViewHolder) holder;
        HolidayModel note = mList.get(position);

        viewHolder.description.setText(note.getDescription());
        viewHolder.date.setText(note.getDate());
        // Displaying dot from HTML character code
        viewHolder.dot.setText(Html.fromHtml("&#8226;"));
        // Changing dot color to random color
        viewHolder.dot.setTextColor(CommonMethods.getRandomMaterialColor(mActivity, "400"));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView description;
        TextView dot;

        public MyViewHolder(View view) {
            super(view);
            dot=view.findViewById(R.id.tv_dot);
            date=view.findViewById(R.id.tv_date);
            description=view.findViewById(R.id.tv_description);
        }
    }

}