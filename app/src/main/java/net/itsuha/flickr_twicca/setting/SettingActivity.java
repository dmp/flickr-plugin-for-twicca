package net.itsuha.flickr_twicca.setting;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.flickrjandroid.oauth.OAuth;

import net.itsuha.flickr_twicca.R;
import net.itsuha.flickr_twicca.utils.PhotosetsUtil;
import net.itsuha.flickr_twicca.utils.PreferenceManager;

import java.util.TreeMap;

import static net.itsuha.flickr_twicca.BuildConfig.DEBUG;

public class SettingActivity extends ActionBarActivity implements AuthDialogFragment.Callback {
    private TreeMap<String, String> mSetsMap = null;
    private static final String LOGTAG = "SettingActivity";
    public static final String ICON_NAME = "icon.dat";
    public static final String ICON = "icon";
    private ImageView mIconView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        // mIconView = (ImageView) findViewById(R.id.img_icon);

        updateUserInfo();
        // prepareSetsPart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserInfo();
        final PreferenceManager pm = PreferenceManager.getInstance();
        if (pm.isAuthenticating()) {
            showAuthDialog();
            pm.setAuthenticating(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ImageView getIconView() {
        return mIconView;
    }

    /**
     * Displays the icon and the user name and sets event listener.
     */
    private void updateUserInfo() {
        final String userName = PreferenceManager.getInstance().getUserName();
        if (!TextUtils.isEmpty(userName)) {
            TextView tv = (TextView) findViewById(R.id.label_user_name);
            tv.setText(userName);
        }

        final Button button = (Button) findViewById(R.id.new_account_button);
        if (TextUtils.isEmpty(userName)) {
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAuthDialog();
                }
            });
        } else {
            button.setVisibility(View.GONE);
        }
    }

    private void showAuthDialog() {
        final PreferenceManager pm = PreferenceManager.getInstance();
        pm.setAuthenticating(true);
        getSupportFragmentManager().beginTransaction()
                .add(AuthDialogFragment.newInstance(this), AuthDialogFragment.TAG)
                .commitAllowingStateLoss();
    }

    private void prepareSetsPart() {
        Spinner setsSpinner = (Spinner) findViewById(R.id.sets_spinner);
        Button updateButton = (Button) findViewById(R.id.update_button);
        if (isFullyFunctionalModel()) {
            PhotosetsUtil util = new PhotosetsUtil(this);
            TreeMap<String, String> setsMap = util.getPhotosetsFromCache();
            if (setsMap != null)
                updateSpinnerWithMap(setsSpinner, setsMap);
            setsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                /**
                 * save selected item
                 */
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    if (DEBUG) {
                        Log.d(LOGTAG, "selected: " + parent.getAdapter().getItem(position));
                    }
                    String sid = resolveIdByTitle((String) parent.getAdapter()
                            .getItem(position), mSetsMap);
                    PreferenceManager.getInstance().saveDefaultSetsId(sid);
                    if (DEBUG) {
                        Log.d(LOGTAG, "saved value: "
                                + PreferenceManager.getInstance()
                                .getDefaultSetsId());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub

                }

            });

            updateButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSetsSpinnerWithFlickr();
                }
            });
        } else {
            setsSpinner.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            TextView tv = (TextView) findViewById(R.id.label_doesnt_support);
            tv.setVisibility(View.VISIBLE);

        }
    }

    private String[] updateSpinnerWithMap(Spinner spinner,
                                          final TreeMap<String, String> setsMap) {
        mSetsMap = setsMap;
        final String[] setsArray = setsMap.values().toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, setsArray);
        adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // TODO: make speficied sets selected
        String defaultSetId = PreferenceManager.getInstance().getDefaultSetsId();
        int defaultPosition = 0;
        for (String setsId : setsMap.keySet()) {
            if (setsId.equals(defaultSetId))
                break;
            defaultPosition++;
        }
        spinner.setSelection(defaultPosition);
        return setsArray;
    }

    private void updateSetsSpinnerWithFlickr() {
        Spinner setsSpinner = (Spinner) findViewById(R.id.sets_spinner);
//		PhotosetsUtil util = new PhotosetsUtil(this);
//		Collection<Photoset> sets = util.getPhotosetsFromFlickr();
//		if (sets == null)
//			return;
        TreeMap<String, String> setsMap = new TreeMap<String, String>();
//		if (sets != null) {
//			for (Photoset set : sets) {
//				setsMap.put(set.getId(), set.getTitle());
//			}
//		}
        // add blank for no setting
        setsMap.put(PreferenceManager.BLANK_SETS_ID, "blank");
        updateSpinnerWithMap(setsSpinner, setsMap);
//		util.savePhotosets(setsMap);
    }

    private String resolveIdByTitle(String title,
                                    TreeMap<String, String> setsMap) {
        for (String id : setsMap.keySet()) {
            if (title.equals(setsMap.get(id)))
                return id;
        }
        return null;
    }

    private boolean isFullyFunctionalModel() {
        // if (VERSION.SDK_INT == VERSION_CODES.DONUT)
        return !(Build.MODEL.equals("IS01") || Build.MODEL.equals("Docomo HT-03A"));
    }

    @Override
    public void onOAuthDone(OAuth result) {
        updateUserInfo();
        onOAuthCancel();
    }

    @Override
    public void onOAuthCancel() {
        final PreferenceManager pm = PreferenceManager.getInstance();
        pm.setAuthenticating(false);
    }
}
