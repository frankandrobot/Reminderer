package com.frankandrobot.reminderer.database;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.frankandrobot.reminderer.helpers.Logger;

import java.util.ArrayList;

/**
 * This class is used to make asynchronous calls to the database.
 * <p/>
 * To use, (1) implement a DatabaseHandler class in the UI class. Implement the
 * desired on*Complete methods. (2) Then call the desired start* method of this
 * class to start the operation.
 * <p/>
 * When the CRUD completes, it will call the postOp (if any) and call the
 * appropriate on*Complete in the handler.
 * <p/>
 * The postOp is optional. The difference between the handler and postOp is that
 * postOp runs in the service thread, while the handler runs in the main UI
 * thread.
 *
 * Recall, an {@link android.app.IntentService} is a {@link android.app.Service} that runs all requests
 * in a single worker thread and automatically stops when there are no more
 * requests.
 */
public class TaskDAOService extends IntentService
{
    private static String TAG = "R:DBService";

    public TaskDAOService(String name)
    {
        super(name);
    }

    public TaskDAOService()
    {
        super(TaskDAOService.class.getSimpleName());
    }

    /**
     * This runs the service to perform the CRUD:
     *
     * (1) calls the db operation
     *
     * (2) runs the postOp operation (if any)
     *
     * (3) returns the result to the handler
     */
    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (Logger.LOGV)
            Log.v(TAG, "starting service to process ops");

        OperationInfo args;

        /*synchronized (opQueue)
        {
            if (opQueue.size() == 0)
            {
                // we're here because someone added an op to the queue so this
                // shouldn't happen
                if (Logger.LOGE)
                    Log.e(TAG, "onHandleIntent: queue size=0");
                return;
            }
            args = opQueue.poll();
        }*/

        /*if (args == null)
        {
            if (Logger.LOGE)
                Log.e(TAG, "onHandleIntent: args is null");
            return;
        }*/

       /* ContentResolver resolver = args.resolver;
        if (resolver != null)
        {

            switch (args.op)
            {
                case Operation.EVENT_ARG_QUERY:
                    Cursor cursor;
                    try
                    {
                        cursor = resolver.query(args.uri,
                                                args.projection,
                                                args.selection,
                                                args.selectionArgs,
                                                args.orderBy);
            *//*
		     * Calling getCount() causes the cursor window to be filled,
		     * which will make the first access on the main thread a lot
		     * faster
		     *//*
                        if (cursor != null)
                        {
                            cursor.getCount();
                        }
                    }
                    catch (Exception e)
                    {
                        Log.w(TAG, e.toString());
                        cursor = null;
                    }

                    args.result = cursor;
                    break;

                case Operation.EVENT_ARG_UPDATE:
                    args.result = resolver.update(args.uri,
                                                  args.values,
                                                  args.selection,
                                                  args.selectionArgs);
                    break;

                case Operation.EVENT_ARG_DELETE:
                    args.result = resolver.delete(args.uri, args.selection,
                                                  args.selectionArgs);
                    break;

                case Operation.EVENT_ARG_INSERT:
                case Operation.EVENT_ARG_BATCH:
                    try
                    {
                        args.result = resolver.applyBatch(args.authority,
                                                          args.cpo);
                    }
                    catch (RemoteException e)
                    {
                        Log.e(TAG, e.toString());
                        args.result = null;
                    }
                    catch (OperationApplicationException e)
                    {
                        Log.e(TAG, e.toString());
                        args.result = null;
                    }
                    break;
            }

            // run posOp operation
            if (args.postOp != null)
                args.postOp.run();

            // passing the original token value back to the caller on top of the
            // event values in arg1.
            Message reply = args.handler.obtainMessage(args.token);
            reply.obj = args;
            reply.arg1 = args.op;

            if (Logger.LOGV)
            {
                Log.d(TAG, "onHandleIntent: op=" + Operation.opToChar(args.op)
                        + ", token=" + reply.what);
            }

            reply.sendToTarget();
        }*/

    }

    /**
     * This is used to tell what operation to perform
     */
    public static class Operation
    {
        static final int EVENT_ARG_QUERY = 1;
        static final int EVENT_ARG_INSERT = 2;
        static final int EVENT_ARG_UPDATE = 3;
        static final int EVENT_ARG_DELETE = 4;
        static final int EVENT_ARG_BATCH = 5;

        protected static char opToChar(int op)
        {
            switch (op)
            {
                case Operation.EVENT_ARG_QUERY:
                    return 'Q';
                case Operation.EVENT_ARG_INSERT:
                    return 'I';
                case Operation.EVENT_ARG_UPDATE:
                    return 'U';
                case Operation.EVENT_ARG_DELETE:
                    return 'D';
                case Operation.EVENT_ARG_BATCH:
                    return 'B';
                default:
                    return '?';
            }
        }
    }

    /**
     * This is the main helper class that contains all the info to perform an
     * operation
     */
    public static class OperationInfo
    {
        // unused fields
        public int token; // Used for cancel
        public Object cookie;
        public String authority;
        // end unused fields?
        public int op; // an Operation value
        public ContentResolver resolver;
        public Handler handler;
        public Uri uri;
        public String[] projection;
        public String selection;
        public String[] selectionArgs;
        public String orderBy;
        public Object result;
        public ContentValues values;
        public ArrayList<ContentProviderOperation> cpo;
        public Runnable postOp;

        /**
         * Minimum fields needed to create an OperationInfo
         *
         * @param op
         * @param resolver
         * @param uri
         * @param handler
         * @param postOp
         */
        OperationInfo(int op, ContentResolver resolver, Uri uri,
                      Handler handler, Runnable postOp)
        {
            this.op = op;
            this.resolver = resolver;
            this.uri = uri;
            this.handler = handler;
            this.postOp = postOp;
            this.authority = TaskProvider.AUTHORITY_NAME;
            cpo = new ArrayList<ContentProviderOperation>();
        }
    }

    public static class DatabaseHandler extends Handler
    {

        @Override
        public void handleMessage(Message msg)
        {
            OperationInfo info = (OperationInfo) msg.obj;

            int token = msg.what;
            int op = msg.arg1;

            if (Logger.LOGV)
            {
                Log.d(TAG, "AsyncQueryService.handleMessage: token=" + token
                        + ", op=" + op + ", result=" + info.result);
            }

            // pass token back to caller on each callback.
            switch (op)
            {
                case Operation.EVENT_ARG_QUERY:
                    onQueryComplete(token, info.cookie, (Cursor) info.result);
                    break;

                case Operation.EVENT_ARG_INSERT:
                    onInsertComplete(token, info.cookie,
                                     (ContentProviderResult[]) info.result);
                    break;

                case Operation.EVENT_ARG_UPDATE:
                    onUpdateComplete(token, info.cookie, (Integer) info.result);
                    break;

                case Operation.EVENT_ARG_DELETE:
                    onDeleteComplete(token, info.cookie, (Integer) info.result);
                    break;

                case Operation.EVENT_ARG_BATCH:
                    onBatchComplete(token, info.cookie,
                                    (ContentProviderResult[]) info.result);
                    break;
            }
        }

        /**
         * Override one or more of these methods
         */

        protected void onBatchComplete(int token, Object cookie,
                                       ContentProviderResult[] result)
        {
        }

        protected void onDeleteComplete(int token,
                                        Object cookie,
                                        Integer result)
        {
        }

        protected void onUpdateComplete(int token,
                                        Object cookie,
                                        Integer result)
        {

        }

        protected void onInsertComplete(int token, Object cookie,
                                        ContentProviderResult[] result)
        {
        }

        protected void onQueryComplete(int token, Object cookie, Cursor result)
        {
        }
    }

}
