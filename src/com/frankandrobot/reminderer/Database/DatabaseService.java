package com.frankandrobot.reminderer.Database;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

/**
 * This class is used to call asynchronous calls to the database
 * 
 */
public class DatabaseService extends IntentService {

    private static ConcurrentLinkedQueue<OperationInfo> commands = new ConcurrentLinkedQueue<OperationInfo>();

    public static class OperationInfo {
	//TODO delete unused fields
	public int token; // Used for cancel
	public int op;
	public ContentResolver resolver;
	public Uri uri;
	public String authority;
	public Handler handler;
	public String[] projection;
	public String selection;
	public String[] selectionArgs;
	public String orderBy;
	public Object result;
	public Object cookie;
	public ContentValues values;
	public ArrayList<ContentProviderOperation> cpo;
	public Runnable postOp;
    }
    
    public DatabaseService(String name) {
	super(name);
	// TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	// TODO Auto-generated method stub

    }

    public static void addTaskToQueue(Context context, Handler handler,
	    ContentValues values, Runnable postOp) {
	OperationInfo info = new OperationInfo();
	info.resolver = context.getContentResolver();
	info.handler = handler;
	info.values = values;
	ContentProviderOperation.Builder b = ContentProviderOperation
		.newInsert(DbColumns.CONTENT_URI).withValues(info.values);
	info.cpo = new ArrayList<ContentProviderOperation>();
	info.cpo.add(b.build());
	info.postOp = postOp;
	commands.add(info);
    }

}
