package in.slanglabs.airtelmock;

import in.slanglabs.airtelmock.R;

/**
 * TODO: Add a class header comment!
 */

public class ActivityDetector {
    public static final String ACTIVITY_MODE = "activity_mode";
    public static final String MODE_MAIN = "main";
    public static final String MODE_BALANCE = "balance";
    public static final String MODE_DOMESTIC_ROAMING = "domestic_roaming";
    public static final String MODE_COUNTRY_ROAMING = "country_roaming";
    public static final String MODE_PRICE_ROAMING = "price_roaming";
    public static final String MODE_CONFIRM_ROAMING = "confirm_roaming";
    public static final String MODE_SECURE = "secure";

    public static int getImage(String mode) {
        int screen;

        switch (mode) {
            case MODE_BALANCE:
                screen = R.drawable.balance;
                break;

            case MODE_DOMESTIC_ROAMING:
                screen = R.drawable.domestic_roaming;
                break;

            case MODE_COUNTRY_ROAMING:
                screen = R.drawable.country_roaming;
                break;

            case MODE_PRICE_ROAMING:
                screen = R.drawable.price_roaming;
                break;

            case MODE_CONFIRM_ROAMING:
                screen = R.drawable.confirm_roaming;
                break;

            case MODE_SECURE:
                screen = R.drawable.secure;
                break;

            case MODE_MAIN:
            default:
                screen = R.drawable.main;
                break;
        }

        return screen;
    }
}
