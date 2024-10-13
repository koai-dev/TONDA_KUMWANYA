package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.BuildConfig;
import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.DisplayAttendancePositionOnMapActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.PresentEmployeesAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.AttendanceModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.LocationModel;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;
import io.reactivex.Flowable;

import static io.reactivex.BackpressureStrategy.DROP;

public class PresentEmployeesFragment extends Fragment implements View.OnClickListener {

    private PresentEmployeesAdapter mAdapter;
    private String monthYear = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
    private String currentDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
    private TextView mTvSelectDate, mTvTotalEmployees, mTvTotalCheckedInEmployees, mTvTotalAbsentEmployees;
    private ArrayList<PersonModel> mPersonModelArrayList = new ArrayList<>();
    private ArrayList<PersonModel> mCheckedInPersonArrayList = new ArrayList<>();
    private ArrayList<PersonModel> mNotCheckedInPersonArrayList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.item_logout);
        item.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_present_employees, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        //https://gist.github.com/ferdy182/d9b3525aa65b5b4c468a
        view.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorLightGray));
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
        setToolbar(view);
        init(view);
        return view;
    }


    private void setToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Employees");
        ((AdminDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AdminDashboardActivity) requireActivity()).onBackPressed();
            }
        });
    }

    private void init(View view) {

        mTvSelectDate = view.findViewById(R.id.tv_select_date);
        mTvTotalEmployees = view.findViewById(R.id.tv_total_employees);
        mTvTotalCheckedInEmployees = view.findViewById(R.id.tv_total_checked_in_employees);
        mTvTotalAbsentEmployees = view.findViewById(R.id.tv_total_absent_employees);
        mTvSelectDate.setText(currentDate);

        RecyclerView rv = view.findViewById(R.id.rv_employee_present);
        rv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        /*DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                layoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);*/
        mAdapter = new PresentEmployeesAdapter(getActivity(),
                new ArrayList<PersonModel>(), new PresentEmployeesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AttendanceModel attendanceModel) {
                Bundle bundle = new Bundle();
                ArrayList<AttendanceModel> attendanceModelArrayList = new ArrayList<>();
                attendanceModelArrayList.add(attendanceModel);
                bundle.putSerializable("AttendanceList", attendanceModelArrayList);
                Intent intent = new Intent(getActivity(), DisplayAttendancePositionOnMapActivity.class);
                intent.putExtras(bundle);
                requireActivity().startActivity(intent);
            }
        });
        rv.setAdapter(mAdapter);

        mTvSelectDate.setOnClickListener(this);
        mTvTotalEmployees.setOnClickListener(this);
        mTvTotalCheckedInEmployees.setOnClickListener(this);
        mTvTotalAbsentEmployees.setOnClickListener(this);
        getPersonList();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_total_employees) {

            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.addData(mPersonModelArrayList);
            }

            if (searchView != null
                    && !searchView.isIconified()) {
                searchView.setQuery("", false);
                searchView.setQueryHint("Employee Name");
            }

            if (getActivity() != null) {
                mTvTotalEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                mTvTotalCheckedInEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
                mTvTotalAbsentEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
            }

        } else if (v.getId() == R.id.tv_total_checked_in_employees) {

            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.addData(mCheckedInPersonArrayList);
            }

            if (searchView != null
                    && !searchView.isIconified()) {
                searchView.setQuery("", false);
                searchView.setQueryHint("Employee Name");
            }

            if (getActivity() != null) {
                mTvTotalEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
                mTvTotalCheckedInEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                mTvTotalAbsentEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
            }

        } else if (v.getId() == R.id.tv_total_absent_employees) {

            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.addData(mNotCheckedInPersonArrayList);
            }

            if (searchView != null
                    && !searchView.isIconified()) {
                searchView.setQuery("", false);
                searchView.setQueryHint("Employee Name");
            }

            if (getActivity() != null) {
                mTvTotalEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
                mTvTotalCheckedInEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
                mTvTotalAbsentEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            }

        } else if (v.getId() == R.id.tv_select_date) {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            Date selectedDate = null;
                            try {
                                selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.US).parse
                                        (String.valueOf(dayOfMonth).concat("-").concat(String.valueOf(monthOfYear + 1)).concat("-").concat(String.valueOf(year)));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            currentDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(selectedDate);
                            monthYear = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(selectedDate);
                            mTvSelectDate.setText(currentDate);
                            getPersonList();
                        }
                    }, mYear, mMonth, mDay);
            // Set the today as maximum date of date picker
            datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            datePickerDialog.show();
        }
    }


    private SearchView searchView;

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setQueryHint("Employee Name");
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    /**
     * User to get worker list
     * here we have get only those users whose work type is 'USER'
     */

    //http://myhexaville.com/2017/08/02/android-firebase-rxjava/
    @SuppressLint("CheckResult")
    private void getPersonList() {

        mPersonModelArrayList.clear();
        mCheckedInPersonArrayList.clear();
        mNotCheckedInPersonArrayList.clear();
        if (getActivity() != null) {
            mTvTotalEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            mTvTotalCheckedInEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
            mTvTotalAbsentEmployees.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
        }
        if (mAdapter != null)
            mAdapter.clear();

        CommonMethods.setFontSizeForPath(String.valueOf(0).concat(" employees"),
                String.valueOf(0), mTvTotalEmployees);
        CommonMethods.setFontSizeForPath(String.valueOf(0).concat(" checked in"),
                String.valueOf(0), mTvTotalCheckedInEmployees);
        CommonMethods.setFontSizeForPath(String.valueOf(0).concat(" not attend"),
                String.valueOf(0), mTvTotalAbsentEmployees);

        if (CommonMethods.isNetworkConnected(requireActivity())) {

            CommonMethods.showProgressDialog(getActivity());
            createEmployeeListFlowable()
                    .flatMap(comments -> Flowable.fromIterable(comments))
                    .flatMap(comment -> createEmployeeAttendancePairFlowable(comment))
                    .toList()
                    .subscribe(pairs -> {
                        int totalCheckedInEmployees = 0;
                        int totalAbsentEmployees = 0;
                        for (Pair<PersonModel, AttendanceModel> pair : pairs) {
                            // done
                            PersonModel personModel = pair.first;
                            AttendanceModel attendanceModel = pair.second;

                            if (attendanceModel != null) {
                                totalCheckedInEmployees++;
                                personModel.setPunchDate(attendanceModel.getPunchDate());
                                personModel.setPunchInTime(attendanceModel.getPunchInTime());
                                personModel.setPunchOutTime(attendanceModel.getPunchOutTime());
                                personModel.setPunchInLatitude(attendanceModel.getPunchInLatitude());
                                personModel.setPunchInLongitude(attendanceModel.getPunchInLongitude());
                                personModel.setPunchOutLatitude(attendanceModel.getPunchOutLatitude());
                                personModel.setPunchOutLongitude(attendanceModel.getPunchOutLongitude());

                                mCheckedInPersonArrayList.add(personModel);
                            } else {
                                totalAbsentEmployees++;

                                mNotCheckedInPersonArrayList.add(personModel);
                            }

                            mPersonModelArrayList.add(personModel);
                        }
                        CommonMethods.setFontSizeForPath(String.valueOf(mPersonModelArrayList.size()).concat(" employees"),
                                String.valueOf(mPersonModelArrayList.size()), mTvTotalEmployees);
                        CommonMethods.setFontSizeForPath(String.valueOf(totalCheckedInEmployees).concat(" checked in"),
                                String.valueOf(totalCheckedInEmployees), mTvTotalCheckedInEmployees);
                        CommonMethods.setFontSizeForPath(String.valueOf(totalAbsentEmployees).concat(" not attend"),
                                String.valueOf(totalAbsentEmployees), mTvTotalAbsentEmployees);

                        if (mAdapter != null)
                            mAdapter.addData(mPersonModelArrayList);

                        CommonMethods.cancelProgressDialog();
                    });
        } else {
            CommonMethods.showConnectionAlert(getActivity());
        }
    }

    // helper methods
    private Flowable<List<PersonModel>> createEmployeeListFlowable() {
        return Flowable.create(e -> AttendanceApplication.refCompanyUserDetails
                .orderByChild("userType").equalTo(ConstantData.TYPE_USER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        ArrayList<PersonModel> comments = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PersonModel value = snapshot.getValue(PersonModel.class);
                            assert value != null;
                            value.setFirebaseKey(snapshot.getKey());
                            comments.add(value);
                        }
                        e.onNext(comments);
                        e.onComplete();
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                    }
                }), DROP);
    }

    private Flowable<Pair<PersonModel, AttendanceModel>> createEmployeeAttendancePairFlowable(PersonModel personModel) {
        return Flowable.create(e -> {
            AttendanceApplication.refCompanyUserAttendanceDetails
                    .child(personModel.getFirebaseKey())
                    .child(monthYear)
                    .orderByChild("punchDate").equalTo(currentDate).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Pair<PersonModel, AttendanceModel> pair = new Pair<>(personModel, ds.getValue(AttendanceModel.class));
                            e.onNext(pair);
                            e.onComplete();
                        }

                    } else {
                        Pair<PersonModel, AttendanceModel> pair = new Pair<>(personModel, null);
                        e.onNext(pair);
                        e.onComplete();
                    }
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                }
            });
        }, DROP);
    }


    @Override
    public void onDestroyView() {
        mPersonModelArrayList.clear();
        mNotCheckedInPersonArrayList.clear();
        mCheckedInPersonArrayList.clear();
        super.onDestroyView();
    }
}