package under_control.home.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import under_control.home.PlayAlarmAty;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, PlayAlarmAty.class);
        i.putExtra("alarm",intent.getSerializableExtra("alarm"));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
