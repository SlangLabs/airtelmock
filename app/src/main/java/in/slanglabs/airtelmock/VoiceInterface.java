package in.slanglabs.airtelmock;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.slanglabs.slang.internal.util.SlangUIUtil;

import in.slanglabs.platform.application.ISlangApplicationStateListener;
import in.slanglabs.platform.application.SlangApplication;
import in.slanglabs.platform.application.SlangApplicationUninitializedException;
import in.slanglabs.platform.application.actions.DefaultResolvedIntentAction;
import in.slanglabs.platform.session.SlangEntity;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;

/**
 * Main code for adding the Slang voice interface to the app
 */

public class VoiceInterface {
    static Application appContext;

    public static void init(final Application appContext) {
        VoiceInterface.appContext = appContext;

        // Initialize Slang with the keys
        SlangApplication
            .initialize(
                appContext,
                R.string.appId,
                R.string.authKey,
                new ISlangApplicationStateListener() {
                    // This is called when Slang is ready
                    @Override
                    public void onInitialized() {
                        try {
                            registerActions();
                        } catch (SlangApplicationUninitializedException e) {
                            Toast.makeText(
                                appContext,
                                "Slang uninitialized - " + e.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                            );
                        }
                    }

                    // Called when Slang failed to initialize
                    @Override
                    public void onInitializationFailed(FailureReason failureReason) {
                        Toast.makeText(
                            appContext,
                            "Could not initialize slang!",
                            Toast.LENGTH_LONG
                        );
                    }
                }
            );

//        SlangApplication.setDefaultContinuationMode(SlangSession.ContinuationMode.PAUSE);
    }

    private static void registerActions() throws SlangApplicationUninitializedException {
        // Register the handler for the "roaming" intent
        SlangApplication.getIntentDescriptor("roaming").setResolutionAction(new DefaultResolvedIntentAction() {
            public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent intent, SlangSession session) {
                intent.getCompletionStatement().overrideAffirmative("It will cost your Rs 149. Please click yes to proceed");
                return session.success();
            }

            @Override
            public SlangSession.Status onEntityUnresolved(SlangEntity entity, final SlangSession session) {
                boolean pause = false;

                // This is called when some entity is unresolved (ie its marked mandatory but
                // Slang could not detect it from the utterance of the user).

                // In this app's schema, "country" and "region" are marked as mandatory entities.
                // But "region" has a higher precedence than "country" (which is determined by the
                // "priority" order - lower numbers have higher precedence)

                // Now if the user had set "region" to "domestic" then we should not prompt the user
                // about the country. So set it to a legal value (in this case "india").
                if (entity.getName().equals("country")) {
                    switch (entity.getParent().getEntity("region").getValue()) {
                        case "international":
                            SlangUIUtil.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    // If country is missing and if its international mode,
                                    // then switch to country check
                                    Intent i = new Intent(appContext, MainActivity.class);

                                    i.putExtra(
                                        ActivityDetector.ACTIVITY_MODE,
                                        ActivityDetector.MODE_COUNTRY_ROAMING
                                    );
                                    appContext.startActivity(i);

                                    // Inform Slang to continue processing
                                    session.success();
                                }
                            });
                            pause = true;
                            break;

                        case "domestic":
                            // Dont prompt for country in domestic mode. Set to a dummy
                            // value
                            entity.resolve("india");
                            break;
                    }
                }

                // If we are doing some async work, ask Slang to suspend until thats done
                // which would be indicated by the app calling "session.success" as done above
                return pause ? session.suspend() : session.success();
            }

            // This is the handler for the intent, which will do the final action (ie
            // start relevant activities)
            @Override
            public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                Intent i = new Intent(appContext, MainActivity.class);

                switch (slangResolvedIntent.getEntity("region").getValue()) {
                    case "international":
                        i.putExtra(
                            ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_CONFIRM_ROAMING
                        );
                        slangSession.setContinuationMode(SlangSession.ContinuationMode.PAUSE);
                        break;

                    case "domestic":
                        i.putExtra(
                            ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_DOMESTIC_ROAMING
                        );
                        break;
                }

                appContext.startActivity(i);
                return slangSession.success();
            }
        });

        // Register for the "balance" intent
        // Not in this case the app dont want to process any unresolved entities and thus
        // does not override "onEntityUnresolved" as done above
        SlangApplication.getIntentDescriptor("balance").setResolutionAction(new DefaultResolvedIntentAction() {
            @Override
            public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent intent, SlangSession session) {
                intent.getCompletionStatement().overrideAffirmative("Showing balance");
                return session.success();
            }

            @Override
            public SlangSession.Status action(SlangResolvedIntent intent, SlangSession session) {
                Intent i = new Intent(appContext, MainActivity.class);

                i.putExtra(
                    ActivityDetector.ACTIVITY_MODE,
                    ActivityDetector.MODE_BALANCE
                );

                appContext.startActivity(i);

//                session.setContinuationMode(SlangSession.ContinuationMode.CONTINUE);

                return session.success();
            }
        });

        // Register handler fo the "secure" intent
        SlangApplication.getIntentDescriptor("secure").setResolutionAction(new DefaultResolvedIntentAction() {
            @Override
            public SlangSession.Status action(SlangResolvedIntent intent, SlangSession session) {
                Intent i = new Intent(appContext, MainActivity.class);

                i.putExtra(
                    ActivityDetector.ACTIVITY_MODE,
                    ActivityDetector.MODE_SECURE
                );

                appContext.startActivity(i);

                return session.success();
            }
        });
    }
}
