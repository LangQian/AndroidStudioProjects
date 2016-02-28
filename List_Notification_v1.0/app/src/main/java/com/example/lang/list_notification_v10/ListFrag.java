/***
  Copyright (c) 2008-2014 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
	
  From _The Busy Coder's Guide to Android Development_
    https://commonsware.com/Android
 */

package com.example.lang.list_notification_v10;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

import java.util.Calendar;

public class ListFrag extends android.app.ListFragment implements
    DialogInterface.OnClickListener {
  private DatabaseHelper db=null;
  private Cursor current=null;
  private AsyncTask task=null;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    
    setHasOptionsMenu(true);
    setRetainInstance(true);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    SimpleCursorAdapter adapter=
        new SimpleCursorAdapter(getActivity(), R.layout.list,
            current, new String[] {
            DatabaseHelper.TITLE,
            DatabaseHelper.TIME_H,
                DatabaseHelper.TIME_M},
            new int[] { R.id.title, R.id.time_h, R.id.time_m },
            0);

    setListAdapter(adapter);

    if (current==null) {
      db=new DatabaseHelper(getActivity());
      task=new LoadCursorTask().execute();
    }
  }


  @Override
  public void onDestroy() {
    if (task != null) {
      task.cancel(false);
    }

    ((CursorAdapter)getListAdapter()).getCursor().close();
    db.close();

    super.onDestroy();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.actions, menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId()==R.id.add) {
      add();
      return(true);
    }

    return(super.onOptionsItemSelected(item));
  }

  private void add() {
    LayoutInflater inflater=getActivity().getLayoutInflater();
    View addView=inflater.inflate(R.layout.add_edit, null);
    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

    builder.setTitle(R.string.add_title).setView(addView)
           .setPositiveButton(R.string.ok, this)
           .setNegativeButton(R.string.cancel, null).show();
  }

  public void onClick(DialogInterface di, int whichButton) {
    ContentValues values=new ContentValues(3);
    AlertDialog dlg=(AlertDialog)di;
    EditText title=(EditText)dlg.findViewById(R.id.title);
    EditText time_h=(EditText)dlg.findViewById(R.id.time_h);
    EditText time_m=(EditText)dlg.findViewById(R.id.time_m);


    values.put(DatabaseHelper.TITLE, title.getText().toString());
    values.put(DatabaseHelper.TIME_H, time_h.getText().toString());
    values.put(DatabaseHelper.TIME_M, time_m.getText().toString());

    Bundle bundle = new Bundle();
// add extras here..
    bundle.putString("title", title.getText().toString());
    bundle.putString("time_h", time_h.getText().toString());
    bundle.putString("time_m", time_m.getText().toString());
    Alarm alarm = new Alarm(getActivity().getApplicationContext(), bundle);

    task=new InsertTask().execute(values);


  }


  abstract private class BaseTask<T> extends AsyncTask<T, Void, Cursor> {
    @Override
    public void onPostExecute(Cursor result) {
      ((CursorAdapter)getListAdapter()).changeCursor(result);
      task=null;
    }

    protected Cursor doQuery() {
      Cursor result=
          db
              .getReadableDatabase()
              .query(DatabaseHelper.TABLE,
                  new String[] {"ROWID AS _id",
                                DatabaseHelper.TITLE,
                                DatabaseHelper.TIME_H,
                          DatabaseHelper.TIME_M},
                  null, null, null, null, DatabaseHelper.TITLE);

      result.getCount();

      return(result);
    }
  }

  private class LoadCursorTask extends BaseTask<Void> {
    @Override
    protected Cursor doInBackground(Void... params) {
      return(doQuery());
    }
  }

  private class InsertTask extends BaseTask<ContentValues> {
    @Override
    protected Cursor doInBackground(ContentValues... values) {
      db.getWritableDatabase().insert(DatabaseHelper.TABLE,
                                      DatabaseHelper.TITLE, values[0]);

      return(doQuery());
    }
  }
}
