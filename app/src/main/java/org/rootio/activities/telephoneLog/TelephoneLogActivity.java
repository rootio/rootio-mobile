package org.rootio.activities.telephoneLog;

import org.rootio.activities.cloud.CloudActivity;
import org.rootio.activities.diagnostics.FrequencyActivity;
import org.rootio.activities.services.ServicesActivity;
import org.rootio.activities.stationDetails.StationActivity;
import org.rootio.activities.telephoneLog.lists.WhitelistActivity;
import org.rootio.handset.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class TelephoneLogActivity extends Activity {

    // private SwipeRefreshLayout swipeContainer;
    private ListView telephoneLogView;
    private TelephoneLogAdapter telephoneLogAdapter;
    private boolean isHomeScreen;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.setContentView(R.layout.telephone_log);
        // this.swipeContainer =
        // (SwipeRefreshLayout)this.findViewById(R.id.swipe_container);
        telephoneLogView = (ListView) this.findViewById(R.id.call_log_lv);
        telephoneLogAdapter = new TelephoneLogAdapter(this);
        telephoneLogView.setAdapter(telephoneLogAdapter);
        this.setTitle("Call Records");
        this.isHomeScreen = this.getIntent().getBooleanExtra("isHomeScreen", true);
        if (!this.isHomeScreen) {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // this.swipeContainer.setOnRefreshListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = this.getMenuInflater();
        if (this.isHomeScreen) {
            // home menu is displayed
        } else {
            menuInflater.inflate(R.menu.activity_telephony, menu);
        }
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()) {
            case R.id.white_list_menu_item:
                Intent intent2 = new Intent(this, WhitelistActivity.class);
                this.startActivity(intent2);
                return true;
            case R.id.station_menu_item:
                intent = new Intent(this, StationActivity.class);
                startActivity(intent);
                return true;
            case R.id.cloud_menu_item:
                intent = new Intent(this, CloudActivity.class);
                startActivity(intent);
                return true;
            case R.id.telephony_menu_item:
                intent = new Intent(this, TelephoneLogActivity.class);
                intent.putExtra("isHomeScreen", false);
                startActivity(intent);
                return true;
            case R.id.frequency_menu_item:
                intent = new Intent(this, FrequencyActivity.class);
                intent.putExtra("isHomeScreen", true);
                startActivity(intent);
                return true;
            case R.id.quit_menu_item:
                // radioRunner.stop
                this.onStop();
                this.finish();
                return true;
            case R.id.services_menu_item:
                intent = new Intent(this, ServicesActivity.class);
                this.startActivity(intent);
                return true;
            default: // handles icon click
                this.finish();
                return true;
        }
    }

    // @Override
    // public void onRefresh() {
    // this.telephoneLogAdapter.notifyDataSetChanged();
    // this.swipeContainer.setRefreshing(false);

    // }
}
