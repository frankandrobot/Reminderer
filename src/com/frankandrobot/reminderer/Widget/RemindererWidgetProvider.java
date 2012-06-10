package com.frankandrobot.reminderer.Widget;

import com.frankandrobot.reminderer.AddTaskActivity;
import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.RemindererActivity;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.RemoteViews;

public class RemindererWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	    int[] appWidgetIds) {
	for (int i = 0; i < appWidgetIds.length; i++) {
	    RemoteViews updateViews = new RemoteViews(context.getPackageName(),
		    R.layout.widget);
	    // Launch add task activity when the user taps on the header
	    final Intent launchAddTaskIntent = new Intent(context, AddTaskActivity.class);
	    launchAddTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //needed
	    final PendingIntent launchAddTaskPendingIntent = PendingIntent
		    .getActivity(context, 0 /* no requestCode */,
			    launchAddTaskIntent, 0 /* no flags */);
	    updateViews.setOnClickPendingIntent(R.id.submit,
		    launchAddTaskPendingIntent);
	    appWidgetManager.updateAppWidget(appWidgetIds[i], updateViews);
	    
	}
	// Push update for this widget to the home screen
	// pin ComponentName thisWidget = new ComponentName(this,
	// WordWidget.class);
	// AppWidgetManager manager = AppWidgetManager.getInstance(this);
	// manager.updateAppWidget(thisWidget, updateViews);

    }

    // // public static class UpdateService extends Service {
    // // @Override
    // // public void onStart(Intent intent, int startId) {
    // // // Build the widget update for today
    // //
    // //
    // // //String wordTitle = matcher.group(1);
    // // updateViews.setTextViewText(R.id.word_title, wordTitle);
    // // updateViews.setTextViewText(R.id.word_type, matcher.group(2));
    // // updateViews.setTextViewText(R.id.definition, matcher.group(3).trim());
    // //
    // // }
    //
    // /**
    // * Build a widget update to show the current Wiktionary
    // * "Word of the day." Will block until the online API returns.
    // */
    // public RemoteViews buildUpdate(Context context) {
    // // Pick out month names from resources
    // Resources res = context.getResources();
    // String[] monthNames = res.getStringArray(R.array.month_names);
    //
    // // Find current month and day
    // Time today = new Time();
    // today.setToNow();
    //
    // // Build today's page title, like "Wiktionary:Word of the day/March 21"
    // String pageName = res.getString(R.string.template_wotd_title,
    // monthNames[today.month], today.monthDay);
    // RemoteViews updateViews = null;
    // String pageContent = "";
    //
    // try {
    // // Try querying the Wiktionary API for today's word
    // SimpleWikiHelper.prepareUserAgent(context);
    // pageContent = SimpleWikiHelper.getPageContent(pageName, false);
    // } catch (ApiException e) {
    // Log.e("WordWidget", "Couldn't contact API", e);
    // } catch (ParseException e) {
    // Log.e("WordWidget", "Couldn't parse API response", e);
    // }
    //
    // // Use a regular expression to parse out the word and its definition
    // Pattern pattern = Pattern.compile(SimpleWikiHelper.WORD_OF_DAY_REGEX);
    // Matcher matcher = pattern.matcher(pageContent);
    // if (matcher.find()) {
    // // Build an update that holds the updated widget contents
    // updateViews = new RemoteViews(context.getPackageName(),
    // R.layout.widget_word);
    //
    // String wordTitle = matcher.group(1);
    // updateViews.setTextViewText(R.id.word_title, wordTitle);
    // updateViews.setTextViewText(R.id.word_type, matcher.group(2));
    // updateViews.setTextViewText(R.id.definition, matcher.group(3).trim());
    //
    // // When user clicks on widget, launch to Wiktionary definition page
    // String definePage = res.getString(R.string.template_define_url,
    // Uri.encode(wordTitle));
    // Intent defineIntent = new Intent(Intent.ACTION_VIEW,
    // Uri.parse(definePage));
    // PendingIntent pendingIntent = PendingIntent.getActivity(context,
    // 0 /* no requestCode */, defineIntent, 0 /* no flags */);
    // updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
    //
    // } else {
    // // Didn't find word of day, so show error message
    // updateViews = new RemoteViews(context.getPackageName(),
    // R.layout.widget_message);
    // CharSequence errorMessage = context.getText(R.string.widget_error);
    // updateViews.setTextViewText(R.id.message, errorMessage);
    // }
    // return updateViews;
    // }
    //
    // @Override
    // public IBinder onBind(Intent intent) {
    // // We don't need to bind to this service
    // return null;
    // }
    // }
}
