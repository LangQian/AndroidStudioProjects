package com.example.lang.list_notification_v10;

import android.app.Activity;
import android.os.Bundle;

public class List extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getFragmentManager().findFragmentById(android.R.id.content)==null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content,
                            new ListFrag()).commit();
        }
    }
}
