package in.gabsinfo.usertimeslotgeofenceattendance.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.gabsinfo.usertimeslotgeofenceattendance.R;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.AdminDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.activity.UserDashboardActivity;
import in.gabsinfo.usertimeslotgeofenceattendance.adapter.NotesAdapter;
import in.gabsinfo.usertimeslotgeofenceattendance.application.AttendanceApplication;
import in.gabsinfo.usertimeslotgeofenceattendance.model.NotesModel;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.CommonMethods;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.ConstantData;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.RecyclerTouchListener;
import in.gabsinfo.usertimeslotgeofenceattendance.utils.SharePreferences;

public class NotesFragment extends Fragment {

    private NotesAdapter mAdapter;
    private List<NotesModel> mNotesList = new ArrayList<>();
    private boolean mIsAdminUser = SharePreferences.getBool(SharePreferences.KEY_IS_ADMIN_USER, SharePreferences.DEFAULT_BOOLEAN);
    private RecyclerView mRv;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.item_logout);
        item.setVisible(false);

        MenuItem itemSearch = menu.findItem(R.id.action_search);
        if (itemSearch != null) {
            itemSearch.setVisible(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        setToolBar(view);
        init(view);
        return view;
    }


    private void setToolBar(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_notes));
        if (getActivity() instanceof AdminDashboardActivity) {
            ((AdminDashboardActivity) requireActivity()).setSupportActionBar(toolbar);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
            Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AdminDashboardActivity) requireActivity()).onBackPressed();
                }
            });
        } else if (getActivity() instanceof UserDashboardActivity) {
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

        mRv = view.findViewById(R.id.recycler_view);
        mRv.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRv.getContext(),
                layoutManager.getOrientation());
        mRv.addItemDecoration(dividerItemDecoration);
        mAdapter = new NotesAdapter(getActivity(), mNotesList);
        mRv.setAdapter(mAdapter);

        if (!mIsAdminUser) {
            fab.hide();
        }
        if (mIsAdminUser) {
            /**
             * On long press on RecyclerView item, open alert dialog
             * with options to choose
             * Edit and Delete
             * */
            mRv.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                    mRv, new RecyclerTouchListener.ClickListener() {
                @Override
                public void onClick(View view, final int position) {
                }

                @Override
                public void onLongClick(View view, int position) {
                    if(mNotesList.get(position).isAllowedToDelete()) {
                        showActionsDialog(position);
                    }
                }
            }));
        }

        //fetch all holidays
        fetchAllNotes();
    }

    /**
     * Fetching all holidays from firebase
     * The received items will be in random order
     */
    private void fetchAllNotes() {

        if (CommonMethods.isNetworkConnected(requireActivity())) {

            CommonMethods.showProgressDialog(getActivity());
            AttendanceApplication.refNotes
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mNotesList.clear();
                            if (dataSnapshot.exists()) {

                                ArrayList<NotesModel> holidayModelArrayList = new ArrayList<>();
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    NotesModel holidayModel = ds.getValue(NotesModel.class);
                                    assert holidayModel != null;
                                    holidayModel.setFirebaseKey(ds.getKey());
                                    holidayModelArrayList.add(holidayModel);
                                }

                                // holidayModelArrayList.addAll(loadDefaultNotes());
                                /**
                                 * Here we reverse array list
                                 */
                                ArrayList<NotesModel> tempElements = new ArrayList<NotesModel>(holidayModelArrayList);
                                Collections.reverse(tempElements);

                                mNotesList.addAll(tempElements);
                            }else{
                                // mNotesList.addAll(loadDefaultNotes());
                            }
                            mAdapter.notifyDataSetChanged();
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
    private void showNoteDialog(final boolean shouldUpdate, final NotesModel note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_notes, null);

        AlertDialog.Builder alertDialogBuilderUserInput =
                new AlertDialog.Builder(requireActivity(), R.style.CustomDialogTheme);
        alertDialogBuilderUserInput.setTitle(!shouldUpdate ? getString(R.string.alert_title_new_note) : getString(R.string.alert_title_update_note));
        alertDialogBuilderUserInput.setView(view);

        final EditText etTitle = view.findViewById(R.id.et_title);
        final EditText holidayDescription = view.findViewById(R.id.et_description);
        if (shouldUpdate && note != null) {
            etTitle.setText(note.getTitle());
            holidayDescription.setText(note.getDescription());
        }

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
                if (TextUtils.isEmpty(etTitle.getText().toString())) {
                    Toast.makeText(getActivity(), getString(R.string.msg_enter_note_title), Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(holidayDescription.getText().toString())) {
                    Toast.makeText(getActivity(), getString(R.string.msg_enter_note_description), Toast.LENGTH_SHORT).show();
                } else {
                    if (CommonMethods.isNetworkConnected(requireActivity())) {
                        // check if user updating note
                        if (shouldUpdate && note != null) {
                            // update note by it's id
                            updateNote(note.getFirebaseKey(), etTitle.getText().toString(), holidayDescription.getText().toString(), position);
                        } else {
                            // create new note
                            createNote(etTitle.getText().toString(), holidayDescription.getText().toString());
                        }
                        alertDialog.dismiss();
                    } else {
                        CommonMethods.showConnectionAlert(getActivity());
                    }
                }
            }
        });
    }

    /**
     * Creating new holiday
     */
    private void createNote(String title, String desc) {

        NotesModel notesModel = new NotesModel();
        notesModel.setTitle(title);
        notesModel.setDescription(desc);
        notesModel.setDate(new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(new Date()));

        String key = AttendanceApplication.refNotes
                .push().getKey();

        assert key != null;
        AttendanceApplication.refNotes
                .child(key)
                .setValue(notesModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonMethods.cancelProgressDialog();
                        // Write was successful!
                        Toast.makeText(getActivity(), getString(R.string.msg_note_added_successfully), Toast.LENGTH_SHORT).show();
                        notesModel.setFirebaseKey(key);
                        // Add new item and notify adapter
                        mNotesList.add(0, notesModel);
                        mAdapter.notifyItemInserted(0);
                        mRv.scrollToPosition(0);

                        CommonMethods.sendCommonNotificationForAllEmployees(getActivity(),
                                ConstantData.NOTIFICATION_TITLE_EXTRA,
                                notesModel.getTitle().concat(" ( NOTE )"),
                                notesModel.getDescription(),
                                "",
                                ConstantData.SUBSCRIBE_ALL_COMPANY_USERS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonMethods.cancelProgressDialog();
                        CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert)
                                , e.getMessage(), getString(R.string.action_ok));
                    }
                });

    }

    /**
     * Updating a holiday
     */
    private void updateNote(String id, String title, String desc, int position) {

        NotesModel notesModel = new NotesModel();
        notesModel.setTitle(title);
        notesModel.setDescription(desc);
        notesModel.setDate(new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(new Date()));

        AttendanceApplication.refNotes
                .child(id)
                .setValue(notesModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonMethods.cancelProgressDialog();
                        // Write was successful!
                        Toast.makeText(getActivity(), getString(R.string.msg_note_updated_successfully), Toast.LENGTH_SHORT).show();

                        NotesModel oldNote = mNotesList.get(position);
                        oldNote.setTitle(notesModel.getTitle());
                        oldNote.setDescription(notesModel.getDescription());
                        oldNote.setDate(notesModel.getDate());

                        mAdapter.notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonMethods.cancelProgressDialog();
                        CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                e.getMessage(), getString(R.string.action_ok));
                    }
                });
    }

    /**
     * Deleting a holiday
     */
    private void deleteNote(final String id, final int position) {
        AttendanceApplication.refNotes
                .child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(), getString(R.string.msg_note_removed), Toast.LENGTH_SHORT).show();

                        // Remove and notify adapter about item deletion
                        mNotesList.remove(position);
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
                    showNoteDialog(true, mNotesList.get(position), position);
                } else {
                    deleteNote(mNotesList.get(position).getFirebaseKey(), position);
                }
            }
        });
        builder.show();
    }

    private ArrayList<NotesModel> loadDefaultNotes(){
        ArrayList<NotesModel> notes = new ArrayList<>();
        String date="15 JAN 2019";

        NotesModel notesModel=new NotesModel();
        notesModel.setTitle("Attendance On Public Holiday");
        notesModel.setDescription("You are not allowed to mark attendance on public holidays. Check your allocated holidays.");
        notesModel.setDate(date);
        notesModel.setAllowedToDelete(false);
        notes.add(notesModel);

        NotesModel notesModel1=new NotesModel();
        notesModel1.setTitle("Absent Day");
        notesModel1.setDescription("Application will count particular day as 'ABSENT' if you/employee left working place before completing your allocated half day hours.");
        notesModel1.setDate(date);
        notesModel1.setAllowedToDelete(false);
        notes.add(notesModel1);

        return notes;
    }

    @Override
    public void onDestroyView() {
        mNotesList.clear();
        super.onDestroyView();
    }
}
