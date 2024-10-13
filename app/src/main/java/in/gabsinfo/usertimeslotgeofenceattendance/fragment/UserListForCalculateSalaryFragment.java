package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.UserListForCalculateSalaryAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;

public class UserListForCalculateSalaryFragment extends Fragment {

    private UserListForCalculateSalaryAdapter mAdapter;
    private int mModuleId=-1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.item_logout);
        item.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list_for_calculate_salary, container, false);
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

        Bundle bundle = getArguments();
        if (bundle!=null){
            mModuleId=bundle.getInt("ModuleId",-1);
            setToolbar(view);
            init(view);
        }
        return view;
    }

    private void setToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_title_person));
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
        RecyclerView rv = view.findViewById(R.id.rv_persons);
        rv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                layoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);

        mAdapter = new UserListForCalculateSalaryAdapter(getActivity(), new ArrayList<PersonModel>(),
                new UserListForCalculateSalaryAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(PersonModel model, int position) {
                        if (getActivity() != null
                                && getActivity() instanceof AdminDashboardActivity) {

                            if(mModuleId >-1) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("PersonModel", model);
                                switch (mModuleId) {
                                    case 6://Calculate Salary
                                        ((AdminDashboardActivity) getActivity()).loadFragment(new UserAttendanceHistoryInCalendarFragment(), bundle);
                                        break;
                                    case 7://View Report
                                        ((AdminDashboardActivity) getActivity()).loadFragment(new EmployeePerformanceFragment(), bundle);
                                        break;
                                    default:
                                        break;
                                }
                            }else{
                                Toast.makeText(getActivity(), "Parent module id not found.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        rv.setAdapter(mAdapter);

        getPersonList();
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) requireActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setQueryHint("Employee Name");
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted by pressing keyboard icon
                mAdapter.getFilter().filter(query);
                searchView.clearFocus();
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
    private void getPersonList() {
        if (CommonMethods.isNetworkConnected(requireActivity())) {

            CommonMethods.showProgressDialog(getActivity());
            AttendanceApplication.refCompanyUserDetails
                    .orderByChild("userType").equalTo(ConstantData.TYPE_USER)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            CommonMethods.cancelProgressDialog();
                            if (dataSnapshot.exists()) {

                                ArrayList<PersonModel> personModelArrayList = new ArrayList<>();
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    PersonModel personModel = ds.getValue(PersonModel.class);
                                    assert personModel != null;
                                    personModel.setFirebaseKey(ds.getKey());
                                    personModelArrayList.add(personModel);
                                }

                                if (mAdapter != null)
                                    mAdapter.addData(personModelArrayList);
                            }
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

    @Override
    public void onDestroyView() {
        if(mAdapter!=null)
            mAdapter.clear();
        super.onDestroyView();
        System.gc();
    }
}