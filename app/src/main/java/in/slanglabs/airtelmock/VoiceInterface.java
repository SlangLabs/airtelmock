package in.slanglabs.airtelmock;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Locale;

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

class VoiceInterface {
    private static final String TAG = VoiceInterface.class.getSimpleName();

    static void init(final Application appContext) {
        try {
            SlangBuddyOptions options = new SlangBuddyOptions.Builder()
                    .setContext(appContext)
                    .setBuddyId(appContext.getResources().getString(getBuddyId()))
                    .setAPIKey(appContext.getResources().getString(getAPIKey()))
                    .setListener(new BuddyListener())
                    .setIntentAction(new V1Action(appContext))
                    .setRequestedLocales(SlangLocale.getSupportedLocales())
                    .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                    .build();
            SlangBuddy.initialize(options);
        } catch (SlangBuddyOptions.InvalidOptionException e) {
            Log.e(TAG, e.getLocalizedMessage());
        } catch (SlangBuddy.InsufficientPrivilegeException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private static int getBuddyId() {
        return BuildConfig.DEBUG ? R.string.appId_dev : R.string.appId_rel;
    }

    private static int getAPIKey() {
        return BuildConfig.DEBUG ? R.string.authKey_dev : R.string.authKey_rel;
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
            Intent i = new Intent(mContext, MainActivity.class);

            i.putExtra(
                ActivityDetector.ACTIVITY_MODE,
                ActivityDetector.MODE_BALANCE
            );

            mContext.startActivity(i);
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

    // This is experimental and unsupported. The APIs used here might change
    private static class MultiStepAction implements SlangMultiStepIntentAction {
        private final Context mContext;

        MultiStepAction(Context context) {
            mContext = context;
        }

        @Override
        public Status action(SlangIntent intent, SlangSession context) {
            Intent i = new Intent(mContext, MainActivity.class);

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

            mContext.startActivity(i);
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
                            mContext,
                            MainActivity.class
                    );
                    i.putExtra(
                            ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_COUNTRY_ROAMING
                    );
                    mContext.startActivity(i);
                    break;

                case "domestic":
                    entity.resolve("india");
                    break;
            }
        }

        private void handlePack(SlangEntity entity) {
            mContext.startActivity(new Intent(mContext, RoamingPacksActivity.class));
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
            try {
                SlangBuddy.getBuiltinUI().setPosition(SlangBuiltinUI.SlangUIPosition.LEFT_BOTTOM);
            } catch (SlangBuddy.UninitializedUsageException e) {
                // Shouldn't happen here
                Log.e(TAG, e.getLocalizedMessage());
            }
        }

        @Override
        public void onInitializationFailed(SlangBuddy.InitializationError e) {}

        @Override
        public void onLocaleChanged(Locale locale) {}

        @Override
        public void onLocaleChangeFailed(Locale locale, SlangBuddy.LocaleChangeError localeChangeError) {}
    }
}
