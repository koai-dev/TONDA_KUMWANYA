package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.DisplayEmployeeTrackingLocationOnMapActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.UserListToTrackEmployeeCurrentLocationsAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class UserListToTrackEmployeeCurrentLocationFragment extends Fragment {

    private UserListToTrackEmployeeCurrentLocationsAdapter mAdapter;
    private Query reference;

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
        View view = inflater.inflate(R.layout.fragment_user_list_to_track_employee_current_location, container, false);
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

        setToolbar(view);
        init(view);
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

        mAdapter = new UserListToTrackEmployeeCurrentLocationsAdapter(getActivity(),  new ArrayList<PersonModel>(),
                new UserListToTrackEmployeeCurrentLocationsAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(PersonModel model, int position) {
                        if (getActivity() != null
                                && getActivity() instanceof AdminDashboardActivity) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("PersonModel", model);
                                Intent intent = new Intent(getActivity(), DisplayEmployeeTrackingLocationOnMapActivity.class);
                                intent.putExtras(bundle);
                                getActivity().startActivity(intent);
                        }
                    }
                });
        rv.setAdapter(mAdapter);

        boolean isDialogAlreadyShownForTracking = SharePreferences.getBool(SharePreferences.KEY_DIALOG_SHOWN_FOR_TRACKING, SharePreferences.DEFAULT_BOOLEAN);
        if (isDialogAlreadyShownForTracking) {
            getPersonList();
        } else {
            String strMessage = "Enable switch for the employee for whom you wants to track his/her current location." +
                    "\n\nKeep in mind that application fetching employees location at the interval of 5 seconds and hence it's drain battery." +
                    "\nSo once you end with your tracking, disable switch for that employee and hence tracking will stop automatically." +
                    "\n\nNote : It's compulsory to have enabled GPS and active internet connection on employees device to enjoy tracking functionality.";
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    requireActivity(), R.style.CustomDialogTheme);
            alertDialogBuilder.setTitle(getString(R.string.title_alert));
            alertDialogBuilder.setMessage(strMessage)
                    .setPositiveButton(getString(R.string.action_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    SharePreferences.setBool(SharePreferences.KEY_DIALOG_SHOWN_FOR_TRACKING, true);
                                    getPersonList();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }


    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) requireActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
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
     * Here we used ValueEventListener because DeviceId is key value
     * for tracking purpose.
     * Here same user can do login from multiple device and hence device id getting changed
     * So we needed latest device id and hence we use ValueEventListener
     */
    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                ArrayList<PersonModel> personModelArrayList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PersonModel personModel = ds.getValue(PersonModel.class);
                    assert personModel != null;
                    if(personModel.getUserType().equals(ConstantData.TYPE_USER)) {
                        personModel.setFirebaseKey(ds.getKey());
                        personModelArrayList.add(personModel);
                    }
                }
                if (mAdapter != null)
                    mAdapter.addData(personModelArrayList);
            }
            CommonMethods.cancelProgressDialog();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            CommonMethods.cancelProgressDialog();
            CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                    databaseError.getMessage(), getString(R.string.action_ok));
        }
    };

    /**
     * User to get worker list
     * here we have get only those users whose work type is 'USER'
     */
    private void getPersonList() {
        if (CommonMethods.isNetworkConnected(requireActivity())) {

            CommonMethods.showProgressDialog(getActivity());
            reference = AttendanceApplication.refCompanyUserDetails;
            reference.addValueEventListener(valueEventListener);
        } else {
            CommonMethods.showConnectionAlert(getActivity());
        }
    }


    @Override
    public void onDestroyView() {
        if (valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
        }
        if (mAdapter != null)
            mAdapter.clear();
        super.onDestroyView();
        System.gc();
    }
}