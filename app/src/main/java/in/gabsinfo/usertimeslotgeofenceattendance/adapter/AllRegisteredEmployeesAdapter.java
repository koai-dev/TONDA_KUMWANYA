package in.gabsinfo.usertimeslotgeofenceattendance.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;

public class AllRegisteredEmployeesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Filterable {
    final int EMPTY_VIEW = 77777;
    private final Activity mActivity;
    private ArrayList<PersonModel> mList;
    private List<PersonModel> mListFiltered;
    private final OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onEdit(PersonModel personModel,int position);
        void onDelete(PersonModel personModel,int position);
    }


    public AllRegisteredEmployeesAdapter(Activity activity,
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
            return new MyViewHolder(layoutInflater.inflate(R.layout.row_employee_list, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) != EMPTY_VIEW) {
            MyViewHolder itemview = (MyViewHolder) holder;
            PersonModel personModel = mListFiltered.get(position);
            if (personModel.getProfileImage() != null
                    && personModel.getProfileImage().trim().length() > 0) {
                CommonMethods.loadImage(mActivity, personModel.getProfileImage(), itemview.ivUserProfileImage,
                        ContextCompat.getDrawable(mActivity, R.drawable.img_module_user_profile));
            } else {
                CommonMethods.loadDefaultImage(mActivity, itemview.ivUserProfileImage,
                        ContextCompat.getDrawable(mActivity, R.drawable.img_module_user_profile));
            }

            itemview.tvPersonName.setText(personModel.getName());
            itemview.tvPersonDesignation.setText(personModel.getDesignation());

            itemview.btnOptions.setOnClickListener(v -> {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(mActivity, itemview.btnOptions);
                //inflating menu from xml resource
                popup.inflate(R.menu.menu_user_profile);
                //adding click listener
                popup.setOnMenuItemClickListener(item -> {
                    if(item.getItemId()==R.id.item_edit){
                        mListener.onEdit(mListFiltered.get(itemview.getAdapterPosition()), itemview.getAdapterPosition());
                    }else if(item.getItemId()==R.id.item_call){
                        CommonMethods.call(mActivity, personModel.getMobileDialerCode() != null ?
                                personModel.getMobileDialerCode().concat(personModel.getMobileNo()) : personModel.getMobileNo());
                    }else if(item.getItemId()==R.id.item_delete){
                        mListener.onDelete(mListFiltered.get(itemview.getAdapterPosition()), itemview.getAdapterPosition());
                    }
                    return false;
                });
                //displaying the popup
                popup.show();
            });
        } else {
            final EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            emptyViewHolder.tvAlertMessage.setText("No Any Registered Employees Found.");
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
        ImageView ivUserProfileImage;
        TextView tvPersonName;
        TextView tvPersonDesignation;
        Button btnOptions;

        public MyViewHolder(View view) {
            super(view);
            ivUserProfileImage =  view.findViewById(R.id.iv_user_profile_image);
            tvPersonName =  view.findViewById(R.id.tv_person_name);
            tvPersonDesignation =  view.findViewById(R.id.tv_person_designation);
            btnOptions =  view.findViewById(R.id.btn_options);
        }
    }

    public void removeItem(int position) {
        PersonModel tempLeaveModel = mListFiltered.get(position);
        //Don't switch position of below 2 lines
        mListFiltered.remove(position);
        mList.remove(tempLeaveModel);
        notifyItemRemoved(position);
    }

    public void clearList() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void addList(ArrayList<PersonModel> personModelArrayList) {
        if(mList!=null)
            mList.clear();
        else
            mList=new ArrayList<>();

        mList.addAll(personModelArrayList);
        notifyDataSetChanged();
    }
}

