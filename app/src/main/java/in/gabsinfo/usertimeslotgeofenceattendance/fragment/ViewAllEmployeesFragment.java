package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.AllRegisteredEmployeesAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.PersonModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;

public class ViewAllEmployeesFragment extends Fragment {

    private AllRegisteredEmployeesAdapter mAdapter;

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
        View view = inflater.inflate(R.layout.fragment_view_all_employees, container, false);
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

        RecyclerView mRv = view.findViewById(R.id.rv);
        mRv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRv.getContext(),
                layoutManager.getOrientation());
        mRv.addItemDecoration(dividerItemDecoration);
        mAdapter=new AllRegisteredEmployeesAdapter(getActivity(), new ArrayList<>(),
                new AllRegisteredEmployeesAdapter.OnItemClickListener() {
                    @Override
                    public void onEdit(PersonModel personModel, int position) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PersonModel", personModel);
                        ((AdminDashboardActivity) requireActivity())
                                .loadFragment(new AddPersonFragment(), bundle);
                    }

                    @Override
                    public void onDelete(PersonModel personModel, int position) {
                        showDialogForDeletePerson(personModel,position);
                    }
                });
        mRv.setAdapter(mAdapter);

        getRegisteredEmployees();
    }

    private void getRegisteredEmployees() {
        if (CommonMethods.isNetworkConnected(requireActivity())) {
            CommonMethods.showProgressDialog(getActivity());
            AttendanceApplication.refCompanyUserDetails
                    .orderByChild("userType").equalTo(ConstantData.TYPE_USER)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                ArrayList<PersonModel> personModelArrayList = new ArrayList<>();
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    PersonModel personModel = ds.getValue(PersonModel.class);
                                    assert personModel != null;
                                    personModel.setFirebaseKey(ds.getKey());
                                    personModelArrayList.add(personModel);
                                }

                                if(mAdapter!=null) {
                                    mAdapter.addList(personModelArrayList);
                                }
                            }
                            CommonMethods.cancelProgressDialog();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            CommonMethods.cancelProgressDialog();
                            Toast.makeText(getActivity(), "Issue found while fetching registered employees due to "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            CommonMethods.showConnectionAlert(getActivity());
        }
    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
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
                // filter recycler view when query submitted
                if(mAdapter!=null)
                    mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if(mAdapter!=null)
                    mAdapter.getFilter().filter(query);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(mAdapter!=null)
                    mAdapter.getFilter().filter("");
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void showDialogForDeletePerson(final PersonModel personModel,final  int position) {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireActivity(),
                R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(getString(R.string.alert_title_delete_person));
        alertDialogBuilder.setMessage(getString(R.string.alert_message_delete_person))
                .setPositiveButton(getString(R.string.action_yes), null)
                .setNegativeButton(getString(R.string.action_no), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (CommonMethods.isNetworkConnected(requireActivity())) {
                            alertDialog.dismiss();

                            CommonMethods.showProgressDialog(getActivity());

                            /**
                             * First we delete User from User Table
                             */
                            AttendanceApplication.refCompanyUserDetails
                                    .child(personModel.getFirebaseKey()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), getString(R.string.msg_person_deleted_successfully), Toast.LENGTH_SHORT).show();

                                            /**
                                             * Here we delete person attendance history
                                             */
                                            AttendanceApplication.refCompanyUserAttendanceDetails
                                                    .child(personModel.getFirebaseKey()).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            CommonMethods.cancelProgressDialog();
                                                            Toast.makeText(getActivity(), getString(R.string.msg_person_attendance_history_removed_successfully), Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            CommonMethods.cancelProgressDialog();
                                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            /**
                                             * Here we delete user profile image
                                             */
                                            if (!TextUtils.isEmpty(personModel.getProfileImageName())) {
                                                AttendanceApplication.storageReference
                                                        .child(personModel.getUserType_mobileNo())
                                                        .child(personModel.getProfileImageName())
                                                        .delete();
                                            }

                                            if (mAdapter != null) {
                                                mAdapter.removeItem(position);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            CommonMethods.cancelProgressDialog();
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            CommonMethods.showConnectionAlert(getActivity());
                        }
                    }
                });

                Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public void onDestroyView() {
        if (mAdapter != null)
            mAdapter.clearList();
        super.onDestroyView();
    }
}