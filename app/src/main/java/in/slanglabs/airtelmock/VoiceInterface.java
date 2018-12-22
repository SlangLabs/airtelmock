package in.slanglabs.airtelmock;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.slanglabs.slang.internal.util.SlangUIUtil;
import com.slanglabs.slang.internal.util.SlangUserConfig;

import java.util.Locale;

import in.slanglabs.platform.application.ISlangApplicationStateListener;
import in.slanglabs.platform.application.SlangApplication;
import in.slanglabs.platform.application.SlangApplicationUninitializedException;
import in.slanglabs.platform.application.SlangLocaleException;
import in.slanglabs.platform.application.actions.DefaultResolvedIntentAction;
import in.slanglabs.platform.session.SlangEntity;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;

/**
 * TODO: Add a class header comment!
 */

public class VoiceInterface {
    static Application appContext;

    public static void init(final Application appContext) {
        VoiceInterface.appContext = appContext;
        try {
            SlangApplication
                    .initialize(
                            appContext,
                            appContext.getString(BuildConfig.DEBUG ? R.string.appId_dev : R.string.appId_rel),
                            appContext.getString(BuildConfig.DEBUG ? R.string.authKey_dev : R.string.authKey_rel),
                            SlangApplication.getSupportedLocales(),
                            SlangApplication.LOCALE_ENGLISH_IN,
                            new ISlangApplicationStateListener() {
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
        } catch (SlangLocaleException e) {}

        SlangApplication.setDefaultContinuationMode(SlangSession.ContinuationMode.CONTINUE);
    }

    private static void registerActions() throws SlangApplicationUninitializedException {
        SlangApplication.getIntentDescriptor("roaming").setResolutionAction(new DefaultResolvedIntentAction() {
            @Override
            public SlangSession.Status onEntityUnresolved(SlangEntity entity, final SlangSession session) {
                boolean pause = false;

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
                } else if (entity.getName().equals("pack")) {
                    SlangUIUtil.runOnUIThread(new Runnable() {
                        public void run() {
                            appContext.startActivity(new Intent(appContext, RoamingPacksActivity.class));
                            session.success();
                        }
                    });
                    pause = true;
                }

                return pause ? session.suspend() : session.success();
            }

            @Override
            public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                Intent i = new Intent(appContext, MainActivity.class);

                switch (slangResolvedIntent.getEntity("region").getValue()) {
                    case "international":
                        i.putExtra(
                            ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_CONFIRM_ROAMING
                        );
                        break;

                    case "domestic":
                        i.putExtra(
                            ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_DOMESTIC_ROAMING
                        );
                        break;
                }

                slangResolvedIntent.getDescriptor().getCompletionStatement().overrideAffirmative(
                        getCompletionPrompt(
                                SlangUserConfig.getLocale(),
                                slangResolvedIntent.getEntity("country").getValue(),
                                slangResolvedIntent.getEntity("pack").getValue()
                        )
                );
                slangSession.setContinuationMode(SlangSession.ContinuationMode.PAUSE);

                appContext.startActivity(i);
                return slangSession.success();
            }
        });

        SlangApplication.getIntentDescriptor("balance").setResolutionAction(new DefaultResolvedIntentAction() {
            @NonNull
            @Override
            public SlangSession.Status action(SlangResolvedIntent intent, SlangSession session) {
                Intent i = new Intent(appContext, MainActivity.class);

                i.putExtra(
                    ActivityDetector.ACTIVITY_MODE,
                    ActivityDetector.MODE_BALANCE
                );

                appContext.startActivity(i);
                return session.success();
            }
        });

        SlangApplication.getIntentDescriptor("secure").setResolutionAction(new DefaultResolvedIntentAction() {
            @NonNull
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

    private static String getCompletionPrompt(Locale locale, String country, String pack) {
        switch(locale.getLanguage()) {
            case "en":
                return getPackName(pack, "en") + " international roaming pack for " + country + " has been activated";

            case "hi":
                return country + " के लिए " + getPackName(pack, "hi") + " की अंतर्राष्ट्रीय रोमिंग पैक सक्रिय है";
        }
        return "";
    }

    private static String getPackName(String pack, String language) {
        switch(pack) {
            case "one_day":
                return language.equals("en") ? "One day" : "एक दिन";

            case "ten_day":
                return language.equals("en") ? "Ten day" : "दस दिन";

            case "thirty_day":
                return language.equals("en") ? "Thirty day" : "तीस दिन";

            case "monthly":
                return language.equals("en") ? "Monthly recurring" : "महीने";
        }
        return "";
    }
}
