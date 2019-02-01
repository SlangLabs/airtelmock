package in.slanglabs.airtelmock;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangBuddyOptions;
import in.slanglabs.platform.SlangEntity;
import in.slanglabs.platform.SlangIntent;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.SlangSession;
import in.slanglabs.platform.action.SlangIntentAction;
import in.slanglabs.platform.action.SlangMultiStepIntentAction;
import in.slanglabs.platform.ui.SlangBuiltinUI;

import static in.slanglabs.platform.action.SlangAction.Status.SUCCESS;

public class VoiceInterface {
    private static VoiceInterface sInstance = new VoiceInterface();
    private static Context sAppContext;

    static void init(final Application appContext) {
        sAppContext = appContext;

        SlangBuddyOptions options = new SlangBuddyOptions.Builder()
                .setContext(appContext)
                .setBuddyId(appContext.getResources().getString(getBuddyId()))
                .setAPIKey(appContext.getResources().getString(getAPIKey()))
                .setListener(new BuddyListener())
                .setIntentAction(new V1Action(sAppContext))
                .setRequestedLocales(SlangLocale.getSupportedLocales())
                .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                .setConfigOverrides(getConfigOverrides())
                .build();
        SlangBuddy.initialize(options);
    }

    private static Map<String, Object> getConfigOverrides() {
        HashMap<String, Object> config = new HashMap<>();
        if (shouldForceDevTier()) {
            config.put("internal.common.io.server_host", "infer-dev.slanglabs.in");
            config.put("internal.common.io.analytics_server_host", "analytics-dev.slanglabs.in");
        }
        return config;
    }

    private static int getBuddyId() {
        return shouldForceDevTier()
                ? R.string.appId_dev
                : BuildConfig.DEBUG ? R.string.appId_dev : R.string.appId_rel;
    }

    private static int getAPIKey() {
        return shouldForceDevTier()
                ? R.string.authKey_dev
                : BuildConfig.DEBUG ? R.string.authKey_dev : R.string.authKey_rel;
    }

    private static boolean shouldForceDevTier() {
        return true;
    }

    private static class V1Action implements SlangIntentAction {
        private final Context mContext;
        V1Action(Context ctx) {
            mContext = ctx;
        }

        @Override
        public Status action(SlangIntent intent, SlangSession session) {
            switch (intent.getName()) {
                case "roaming":
                    handleRoaming(intent);
                    break;

                case "balance":
                    handleBalance(intent);
                    break;
            }

            return SUCCESS;
        }

        private void handleBalance(SlangIntent intent) {
            Intent i = new Intent(sAppContext, MainActivity.class);

            i.putExtra(
                ActivityDetector.ACTIVITY_MODE,
                ActivityDetector.MODE_BALANCE
            );

            sAppContext.startActivity(i);
        }

        private void handleRoaming(SlangIntent intent) {
            SlangEntity region = intent.getEntity("region");
            if (!region.isResolved()) {
                handleRegion(intent, region);
                return;
            }

            if (region.getValue().equals("domestic")) {
                handleDomestic(intent, region);
                return;
            }

            // International roaming
            SlangEntity country = intent.getEntity("country");
            if (!country.isResolved()) {
                handleInternational(intent, country);
                return;
            }

            SlangEntity pack = intent.getEntity("pack");
            if (!pack.isResolved()) {
                handlePack(intent, pack);
                return;
            }

            handleIntent(intent);
        }

        private void handleRegion(SlangIntent intent, SlangEntity region) {
            intent.setCompletionStatement(region.getStatement());
        }

        private void handleDomestic(SlangIntent intent, SlangEntity entity) {
            mContext.startActivity(getIntent(ActivityDetector.MODE_DOMESTIC_ROAMING));
        }

        private void handleInternational(SlangIntent intent, SlangEntity country) {
            intent.setCompletionStatement(country.getStatement());
            mContext.startActivity(getIntent(ActivityDetector.MODE_COUNTRY_ROAMING));
        }

        private void handlePack(SlangIntent intent, SlangEntity pack) {
            intent.setCompletionStatement(pack.getStatement());
            mContext.startActivity(new Intent(mContext, RoamingPacksActivity.class));
        }

        private void handleIntent(SlangIntent intent) {
            mContext.startActivity(getIntent(ActivityDetector.MODE_CONFIRM_ROAMING));
        }

        private Intent getIntent(String dest) {
            Intent i = new Intent(mContext, MainActivity.class);
            i.putExtra(ActivityDetector.ACTIVITY_MODE, dest);
            return i;
        }
    }

    // This is still experimental and unsupported. The APIs used here might change
    private static class MultiStepAction implements SlangMultiStepIntentAction {
        @Override
        public Status action(SlangIntent intent, SlangSession context) {
            Intent i = new Intent(sAppContext, MainActivity.class);

            switch (intent.getEntity("region").getValue()) {
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

            sAppContext.startActivity(i);
            return SUCCESS;
        }

        @Override
        public void onIntentResolutionBegin(SlangIntent intent, SlangSession context) {}

        @Override
        public Status onEntityUnresolved(final SlangEntity entity, final SlangSession context) {
            if (entity.getIntent().getName().equals("roaming")) {
                if (entity.getName().equals("country")) {
                    handleCountry(entity);
                } else if (entity.getName().equals("pack")) {
                    handlePack(entity);
                }
            }
            return SUCCESS;
        }

        private void handleCountry(SlangEntity entity) {
            switch(entity.getIntent().getEntity("region").getValue()) {
                case "international":
                    Intent i = new Intent(
                            sAppContext,
                            MainActivity.class
                    );
                    i.putExtra(
                            ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_COUNTRY_ROAMING
                    );
                    sAppContext.startActivity(i);
                    break;

                case "domestic":
                    entity.resolve("india");
                    break;
            }
        }

        private void handlePack(SlangEntity entity) {
            sAppContext.startActivity(new Intent(sAppContext, RoamingPacksActivity.class));
        }

        @Override
        public Status onEntityResolved(SlangEntity entity, SlangSession context) {
            return SUCCESS;
        }

        @Override
        public void onIntentResolutionEnd(SlangIntent intent, SlangSession context) {}

    }

    private static class BuddyListener implements SlangBuddy.Listener {
        @Override
        public void onInitialized() {
            SlangBuddy.getBuiltinUI().setPosition(SlangBuiltinUI.SlangTriggerPosition.LEFT_BOTTOM);
        }

        @Override
        public void onInitializationFailed(SlangBuddy.InitializationError e) {}
    }
}
