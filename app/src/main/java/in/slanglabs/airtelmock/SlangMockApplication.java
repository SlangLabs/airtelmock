package in.slanglabs.airtelmock;

import android.app.Application;

/**
 * TODO: Add a class header comment!
 */

public class SlangMockApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Create a local Client object
        VoiceInterface.init(this);
    }
}
