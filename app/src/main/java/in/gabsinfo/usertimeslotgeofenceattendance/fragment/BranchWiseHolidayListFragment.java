package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.BranchWiseHolidayListAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.HolidayModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.RecyclerTouchListener;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class BranchWiseHolidayListFragment extends Fragment {

    private BranchWiseHolidayListAdapter mAdapter;
    private List<HolidayModel> holidaysList = new ArrayList<>();
    private boolean mIsAdminUser=SharePreferences.getBool(SharePreferences.KEY_IS_ADMIN_USER,SharePreferences.DEFAULT_BOOLEAN);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.item_logout);
        item.setVisible(false);

        MenuItem itemSearch=menu.findItem(R.id.action_search);
        if(itemSearch!=null) {
            itemSearch.setVisible(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_branch_wise_holiday_list, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //https://gist.github.com/ferdy182/d9b3525aa65b5b4c468a
        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), android.R.color.white));
        // To run the animation as soon as the view is layout in the view hierarchy we add this
        // listener and remove it
        // as soon as it runs to prevent multiple animations if the view changes bounds
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                int cx = 20;
                int cy = 20;

                // get the hypothenuse so the radius is from one corner to the other
                int radius = (int) Math.hypot(right, bottom);

                Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                reveal.setInterpolator(new DecelerateInterpolator(2f));
                reveal.setDuration(1000);
                reveal.start();
            }
        });

        setToolBar(view);
        init(view);
        return view;
    }

    private void setToolBar(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_holidays));
        if(getActivity() instanceof AdminDashboardActivity) {
            ((AdminDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AdminDashboardActivity) requireActivity()).onBackPressed();
                }
            });
        }else if(getActivity() instanceof UserDashboardActivity) {
            ((UserDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
            Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((UserDashboardActivity) requireActivity()).onBackPressed();
                }
            });
        }
    }
    private void init(View view) {

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });

        RecyclerView rv= view.findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                layoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        mAdapter = new BranchWiseHolidayListAdapter(getActivity(), holidaysList);
        rv.setAdapter(mAdapter);

        if(!mIsAdminUser) {
            fab.hide();
        }
        if(mIsAdminUser) {
            /**
             * On long press on RecyclerView item, open alert dialog
             * with options to choose
             * Edit and Delete
             * */
            rv.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                    rv, new RecyclerTouchListener.ClickListener() {
                @Override
                public void onClick(View view, final int position) {
                }

                @Override
                public void onLongClick(View view, int position) {
                    showActionsDialog(position);
                }
            }));
        }

        //fetch all holidays
        fetchAllHolidays();
    }

    /**
     * Fetching all holidays from firebase
     * The received items will be in random order
     */
    private void fetchAllHolidays() {

        if (CommonMethods.isNetworkConnected(requireActivity())) {

            CommonMethods.showProgressDialog(getActivity());
            AttendanceApplication.refPublicHoliday
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                ArrayList<HolidayModel> holidayModelArrayList = new ArrayList<>();
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    HolidayModel holidayModel = ds.getValue(HolidayModel.class);
                                    assert holidayModel != null;
                                    holidayModel.setFirebaseKey(ds.getKey());
                                    holidayModelArrayList.add(holidayModel);
                                }

                                /**
                                 * Here we reverse array list
                                 */
                                ArrayList<HolidayModel> tempElements = new ArrayList<HolidayModel>(holidayModelArrayList);
                                Collections.reverse(tempElements);

                                holidaysList.clear();
                                holidaysList.addAll(tempElements);
                                mAdapter.notifyDataSetChanged();
                            }
                            CommonMethods.cancelProgressDialog();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            CommonMethods.cancelProgressDialog();
                            CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                    databaseError.getMessage(), getString(R.string.action_ok));
                        }
                    });
        } else {
            CommonMethods.showConnectionAlert(getActivity());
        }
    }

    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private void showNoteDialog(final boolean shouldUpdate, final HolidayModel note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_holiday, null);

        AlertDialog.Builder alertDialogBuilderUserInput =
                new AlertDialog.Builder(requireActivity(),R.style.CustomDialogTheme);
        alertDialogBuilderUserInput.setTitle(!shouldUpdate ? getString(R.string.alert_title_new_holiday) : getString(R.string.alert_title_update_holiday));
        alertDialogBuilderUserInput.setView(view);

        final TextView holidayDate = view.findViewById(R.id.tv_holiday_date);
        final EditText holidayDescription = view.findViewById(R.id.tv_holiday_description);
        if (shouldUpdate && note != null) {
            holidayDate.setText(note.getDate());
            holidayDescription.setText(note.getDescription());
        }

        holidayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(requireActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month);
                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        holidayDate.setText(new SimpleDateFormat
                                (ConstantData.DATE_FORMAT, Locale.US).format(c.getTime()));

                    }
                }, year, month, day);
                dpd.show();
            }
        });

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? getString(R.string.action_update) : getString(R.string.action_save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton(getString(R.string.action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(holidayDate.getText().toString())) {
                    Toast.makeText(getActivity(), getString(R.string.msg_set_date), Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(holidayDescription.getText().toString())) {
                    Toast.makeText(getActivity(), getString(R.string.msg_enter_holiday_description), Toast.LENGTH_SHORT).show();
                } else {
                    if (CommonMethods.isNetworkConnected(requireActivity())) {
                        // check if user updating note
                        if (shouldUpdate && note != null) {
                            // update note by it's id
                            updateNote(note.getFirebaseKey(), holidayDate.getText().toString(), holidayDescription.getText().toString(), position);
                        } else {
                            // create new note
                            createNote(holidayDate.getText().toString(), holidayDescription.getText().toString());
                        }
                        alertDialog.dismiss();
                    }else {
                        CommonMethods.showConnectionAlert(getActivity());
                    }
                }
            }
        });
    }

    /**
     * Creating new holiday
     */
    private void createNote(String date,String desc) {
        Date selectedDate=null;
        try {
            selectedDate=new SimpleDateFormat
                    (ConstantData.DATE_FORMAT, Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HolidayModel holidayModel=new HolidayModel();
        holidayModel.setDate(date);
        holidayModel.setDescription(desc);

        if(selectedDate!=null){
            holidayModel.setYear(String.valueOf(selectedDate.getYear()));

            SimpleDateFormat monthYearFormat = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);
            holidayModel.setYear(yearFormat.format(selectedDate));
            holidayModel.setMonth(monthFormat.format(selectedDate));
            holidayModel.setMonthYear(monthYearFormat.format(selectedDate));
        }

        String key = AttendanceApplication.refPublicHoliday
                .push().getKey();

        assert key != null;
        AttendanceApplication.refPublicHoliday
                .child(key)
                .setValue(holidayModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonMethods.cancelProgressDialog();
                        // Write was successful!
                        Toast.makeText(getActivity(), getString(R.string.msg_holiday_added_successfully), Toast.LENGTH_SHORT).show();
                        holidayModel.setFirebaseKey(key);
                        // Add new item and notify adapter
                        holidaysList.add(0, holidayModel);
                        mAdapter.notifyItemInserted(0);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonMethods.cancelProgressDialog();
                        CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                String.format(getString(R.string.msg_issue_while_updating_person_detail), e.getMessage()), getString(R.string.action_ok));
                    }
                });

    }
    /**
     * Updating a holiday
     */
    private void updateNote(String id, String date, String desc, int position) {

        Date selectedDate=null;
        try {
            selectedDate=new SimpleDateFormat
                    (ConstantData.DATE_FORMAT, Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HolidayModel holidayModel=new HolidayModel();
        holidayModel.setDate(date);
        holidayModel.setDescription(desc);

        if(selectedDate!=null){
            holidayModel.setYear(String.valueOf(selectedDate.getYear()));

            SimpleDateFormat monthYearFormat = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);
            holidayModel.setYear(yearFormat.format(selectedDate));
            holidayModel.setMonth(monthFormat.format(selectedDate));
            holidayModel.setMonthYear(monthYearFormat.format(selectedDate));
        }

        AttendanceApplication.refPublicHoliday
                .child(id)
                .setValue(holidayModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonMethods.cancelProgressDialog();
                        // Write was successful!
                        Toast.makeText(getActivity(), getString(R.string.msg_holiday_updated_successfully), Toast.LENGTH_SHORT).show();

                        holidaysList.remove(position);

                        holidayModel.setFirebaseKey(id);
                        holidaysList.add(holidayModel);

                        // Update item and notify adapter
                        holidaysList.set(position, holidayModel);
                        mAdapter.notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonMethods.cancelProgressDialog();
                        CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                String.format(getString(R.string.msg_issue_while_updating_person_detail), e.getMessage()), getString(R.string.action_ok));
                    }
                });
    }

    /**
     * Deleting a holiday
     */
    private void deleteNote(final String id, final int position) {
        AttendanceApplication.refPublicHoliday
                .child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(), getString(R.string.msg_holiday_removed), Toast.LENGTH_SHORT).show();

                        // Remove and notify adapter about item deletion
                        holidaysList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{getString(R.string.action_edit), getString(R.string.action_delete)};

        AlertDialog.Builder builder =
                new AlertDialog.Builder(requireActivity(),
                        R.style.CustomDialogTheme);
        builder.setTitle(getString(R.string.alert_title_choose_option));
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, holidaysList.get(position), position);
                } else {
                    deleteNote(holidaysList.get(position).getFirebaseKey(), position);
                }
            }
        });
        builder.show();
    }
}
