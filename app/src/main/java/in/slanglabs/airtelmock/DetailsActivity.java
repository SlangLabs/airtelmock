package in.slanglabs.airtelmock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import in.slanglabs.airtelmock.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String actvity_mode = intent.getStringExtra(ActivityDetector.ACTIVITY_MODE) != null
            ? intent.getStringExtra(ActivityDetector.ACTIVITY_MODE)
            : ActivityDetector.MODE_CONFIRM_ROAMING;

        int screen = ActivityDetector.getImage(actvity_mode);
        ImageView image = (ImageView) findViewById(R.id.image);

        if (image != null) {
            image.setImageResource(screen);
        }

        return;
    }
}
