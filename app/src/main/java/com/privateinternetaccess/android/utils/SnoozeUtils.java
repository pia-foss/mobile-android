/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.android.utils;

import static android.os.Looper.getMainLooper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.model.events.SnoozeEvent;
import com.privateinternetaccess.android.receivers.OnSnoozeReceiver;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SnoozeUtils {
    private static final int SNOOZE_REQUEST_CODE = 24;

    public static String getWakeupTime(Context context) {
        Calendar calendar = Calendar.getInstance();

        long snoozeTime = PiaPrefHandler.getLastSnoozeTime(context);

        if (snoozeTime > 0 && snoozeTime > System.currentTimeMillis()) {
            calendar.setTimeInMillis(PiaPrefHandler.getLastSnoozeTime(context));
            SimpleDateFormat format = new SimpleDateFormat("h:mm");

            return format.format(calendar.getTime());
        }

        return "";
    }

    public static boolean hasActiveAlarm(Context context) {
        if (PiaPrefHandler.getLastSnoozeTime(context) < System.currentTimeMillis()) {
            return false;
        }

        return true;
    }

    public static void setSnoozeAlarm(Context context, long time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                final Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
            } else {
                setSnooze(context, alarmManager, time);
            }
        } else {
            setSnooze(context, alarmManager, time);
        }
    }

    private static void setSnooze(Context context, AlarmManager alarmManager, long time) {
        Intent intent = new Intent(context, OnSnoozeReceiver.class);
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT;
        } else {
            flags = PendingIntent.FLAG_CANCEL_CURRENT;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, SNOOZE_REQUEST_CODE, intent, flags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, time, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC, time, pendingIntent);
        }

        PiaPrefHandler.setLastSnoozeTime(context, time);
        EventBus.getDefault().post(new SnoozeEvent(false));
    }

    public static void resumeVpn(Context context, boolean forceStart) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, OnSnoozeReceiver.class);
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE;
        } else {
            flags = PendingIntent.FLAG_NO_CREATE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, SNOOZE_REQUEST_CODE, intent, flags);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        PiaPrefHandler.setLastSnoozeTime(context, 0);

        if(!PIAFactory.getInstance().getVPN(context).isVPNActive() && forceStart) {
            new Handler(getMainLooper()).post(() -> PIAFactory.getInstance().getVPN(context).start());
        }

        EventBus.getDefault().post(new SnoozeEvent(true));
    }
}
