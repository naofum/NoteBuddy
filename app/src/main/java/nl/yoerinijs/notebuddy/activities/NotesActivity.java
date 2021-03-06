package nl.yoerinijs.notebuddy.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.yoerinijs.notebuddy.R;
import nl.yoerinijs.notebuddy.files.DirectoryReader;
import nl.yoerinijs.notebuddy.files.TextfileReader;
import nl.yoerinijs.notebuddy.files.TextfileRemover;
import nl.yoerinijs.notebuddy.security.SaltGenerator;
import nl.yoerinijs.notebuddy.storage.KeyValueDB;

/**
 * This class displays all notes
 */
public class NotesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Final variables
    private static final String PACKAGE_NAME = "nl.yoerinijs.notebuddy.activities";
    private static final String LOGIN_ACTIVITY = "LoginActivity";
    private static final String CREDITS_ACTIVITY = "CreditsActivity";
    private static final String EDIT_NOTE_ACTIVITY = "EditNoteActivity";
    private static final String NOTES_ACTIVITY = "NotesActivity";
    private static final String SETUP_ACTIVITY = "SetupActivity";
    private static final String SETUP_SECRET_ACTIVITY = "SetupSecretActivity";
    private static final String LOG_TAG = "Notes Activity";

    // UI references
    private ArrayList mNoteNames;
    private Context mContext;

    // Commonly used variables
    private String location;
    private KeyValueDB keyValueDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Set up the notes screen
        mContext = this;
        final ListView listNotes = (ListView) findViewById(R.id.listNotes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up a random salt if it is not set
        // Salt is needed for encrypting and decrypting the notes
        SaltGenerator sg = new SaltGenerator();
        sg.getSalt(mContext);

        // Set up key value storage
        keyValueDB = new KeyValueDB();

        // Get absolute internal storage path
        location = getFilesDir().getAbsolutePath();

        // Log path
        Log.d(LOG_TAG, "Location: " + location);

        // Retrieve files from internal storage
        DirectoryReader dr = new DirectoryReader();
        try {
            mNoteNames = dr.getFileNames(location, 0);
            if (mNoteNames != null) {
                final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, mNoteNames);
                listNotes.setAdapter(adapter);
            }
        } catch (Exception e) {
            // Let the user know that the app cannot read the files
            Toast.makeText(getApplicationContext(), getString(R.string.error_cannot_read_files) + ".", Toast.LENGTH_SHORT).show();

            // Log exception
            Log.d(LOG_TAG, e.getMessage());
        }

        // When clicked on a note, start the Edit Note activity
        // and send the note body and the node title to the new activity
        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get selected note name
                String selectedNoteTitle = mNoteNames.get((int)id).toString();

                // Get text from selected note name
                TextfileReader t = new TextfileReader();
                String note = t.getText(location, selectedNoteTitle, mContext);

                // Start activity to edit the note
                // Pass note and selected note name
                startActvitiy(EDIT_NOTE_ACTIVITY, false, note, selectedNoteTitle);
            }
        });

        // When clicked on the add button, start the edit note activity
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.addNoteButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Proceed to add note activity
                startActvitiy(EDIT_NOTE_ACTIVITY, false, null, null);
            }
        });

        // The navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * The navigation menu
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here
        int id = item.getItemId();

        // Credits item
        if (id == R.id.nav_credits) {
            // Proceed to credits activity
            startActvitiy(CREDITS_ACTIVITY, false, null, null);

        // Erase existing notes item
        } else if (id == R.id.nav_erase) {
            // Display warning dialog
            new AlertDialog.Builder(mContext)
                    .setTitle(getString(R.string.dialog_title_delete_notes))
                    .setMessage(getString(R.string.dialog_question_delete_notes))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Log delete notes
                            Log.d(LOG_TAG, "Delete notes");

                            // Delete notes
                            TextfileRemover tr = new TextfileRemover();
                            tr.deleteAllFiles(location);

                            // Notify user and ourselves
                            Toast.makeText(getApplicationContext(), getString(R.string.success_deleted) + ".", Toast.LENGTH_SHORT).show();
                            Log.d(LOG_TAG, "Notes deleted");

                            // Go to notes activity
                            startActvitiy(NOTES_ACTIVITY, true, null, null);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Log notes not deleted
                            Log.d(LOG_TAG, "Notes not deleted");
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        // Erase credentials and secret note item
        } else if (id == R.id.nav_credentials) {
            // Display warning dialog
            new AlertDialog.Builder(mContext)
                    .setTitle(getString(R.string.dialog_title_delete_credentials))
                    .setMessage(getString(R.string.dialog_question_delete_credentials))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Log delete notes
                            Log.d(LOG_TAG, "Delete credentials");

                            // Response variables
                            String message;
                            String logMessage;

                            // Try to delete credentials
                            try {
                                keyValueDB.deleteCredentials(mContext);
                                message = getString(R.string.success_deleted);
                                logMessage = "Credentials deleted";
                            } catch (NoSuchAlgorithmException n) {
                                message = getString(R.string.error_cannot_delete);
                                logMessage = "Cannot delete credentials";
                            }

                            // Provide response to the user and to ourselves
                            Toast.makeText(getApplicationContext(), message + ".", Toast.LENGTH_SHORT).show();
                            Log.d(LOG_TAG, logMessage);

                            // Go to notes activity
                            startActvitiy(SETUP_ACTIVITY, true, null, null);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Log credentials not deleted
                            Log.d(LOG_TAG, "Credentials not deleted");
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        // Kill switch item
        } else if (id == R.id.nav_kill) {
            // Display warning dialog
            new AlertDialog.Builder(mContext)
                    .setTitle(getString(R.string.dialog_title_delete_everything))
                    .setMessage(getString(R.string.dialog_question_delete_everything))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Log delete notes
                            Log.d(LOG_TAG, "Delete credentials");

                            // Delete all settings
                            keyValueDB.clearSharedPreference(mContext);

                            // Delete notes
                            TextfileRemover tr = new TextfileRemover();
                            tr.deleteAllFiles(location);

                            // Notify user and ourselves
                            Toast.makeText(getApplicationContext(), getString(R.string.success_deleted) + ".", Toast.LENGTH_SHORT).show();
                            Log.d(LOG_TAG, "Everything deleted");

                            // Go to notes activity
                            startActvitiy(SETUP_ACTIVITY, true, null, null);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Log credentials not deleted
                            Log.d(LOG_TAG, "Credentials not deleted");
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        // Lock item
        } else if (id == R.id.nav_lock) {
            // Proceed to main activity to force login
            startActvitiy(LOGIN_ACTIVITY, true, null, null);

        // Secret item
        } else if (id == R.id.nav_secret) {
            // Proceed to setup secret activity
            startActvitiy(SETUP_SECRET_ACTIVITY, false, null, null);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Start a new activity
     * Boolean finish will stop the current activity for security purposes
     * @param activity
     * @param finish
     * @param note
     * @param selectedNoteName
     */
    private void startActvitiy(String activity, Boolean finish, String note, String selectedNoteName) {
        // Log activity
        Log.d(LOG_TAG, "Proceed to " + activity);

        // Construct activity
        Intent intent = new Intent();
        intent.setClassName(mContext, PACKAGE_NAME + "." + activity);

        // Check if a note and a note name are given
        // If true, add them to the activity
        if (note != null && selectedNoteName != null) {
            intent.putExtra("SELECTED_NOTE", note);
            intent.putExtra("SELECTED_NOTE_FILENAME", selectedNoteName);
        }

        // Start activity
        startActivity(intent);

        // Close activity for security purposes
        if (finish) {
            finish();
        }
    }

    /**
     * Stable array adapter for displaying the notes
     */
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
