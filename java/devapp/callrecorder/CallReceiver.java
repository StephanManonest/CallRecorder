package devapp.callrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {

    public CallReceiver() {
    }

    private String ON_OF = "onOff", ALL_NUMBER_CALL = "allcall", SERVICE_NUMBER_CALL = "serviceCall", BLACK_LIST_NUMBER_CALL = "blackListCall", MODE_CODE = "mode";
    boolean onOff = false, allCall = false, serviceCall = false, blackListCall = false; int modeCode=0;
    boolean [] options = null;
    SharedPreferences sharedPreferences = null;
    String NUMBER = "number";
    String RECORD = "record";
    String OPTINS = "options";
    SharedPreferences sharedPreferencesIntentService = null;
    SharedPreferences.Editor editor = null;
    Bundle bundle = null;
    String phoneState = null;
    String phoneNumber = null;
    Intent callService = null;
    boolean intentService = true;
    boolean record = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPreferences = context.getSharedPreferences("OPTIONS", Context.MODE_PRIVATE);
        onOff = sharedPreferences.getBoolean(ON_OF, false);
        allCall = sharedPreferences.getBoolean(ALL_NUMBER_CALL, false);
        serviceCall = sharedPreferences.getBoolean(SERVICE_NUMBER_CALL, false);
        blackListCall = sharedPreferences.getBoolean(BLACK_LIST_NUMBER_CALL, false);
        modeCode = sharedPreferences.getInt(MODE_CODE, 0);
        options = new boolean[]{allCall, serviceCall, blackListCall};
        sharedPreferencesIntentService = context.getSharedPreferences("INTENT_SERVICE", Context.MODE_PRIVATE);
        intentService = sharedPreferencesIntentService.getBoolean("intent_service", true);
        editor = sharedPreferencesIntentService.edit();

        if (onOff && (allCall || serviceCall || blackListCall)) {

            if (intent != null) {
                bundle = intent.getExtras();
                phoneState = bundle.getString(TelephonyManager.EXTRA_STATE);
                callService = new Intent(context, CallService.class);

                // 2 defa yollamasını engellemek için kontrol: subscriton id
                int  subId =  intent.getIntExtra("subscription", Integer.MIN_VALUE);
                if (subId <  Integer.MAX_VALUE) {

                    if ((intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) || (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING))) {

                        if ((intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))) {
                            phoneNumber = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
                        } else {
                            phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        }
                        callService.putExtra(OPTINS, options);
                        callService.putExtra(NUMBER, phoneNumber);
                        if (intentService) {
                            context.startService(callService);
                        }
                    } else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) || phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                            record = true;
                            editor.putBoolean("intent_service", false);
                            editor.apply();
                            callService.putExtra(RECORD, record);
                            callService.putExtra(MODE_CODE,modeCode);
                            if (intentService) {
                                context.startService(callService);
                            }
                        } else {
                            record = false;
                            editor.putBoolean("intent_service", true);
                            editor.apply();
                            context.startService(callService);
                        }
                    }
                }
            }
        }
    }
}