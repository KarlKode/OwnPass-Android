package com.ownpass.android;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by leo on 10/25/13.
 */
public class CredentialsListActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials_list);

        final ArrayList<Credential> creds = new ArrayList<Credential>();

        creds.add(new Credential(123, "www.facebook.com", "leo@foo.at"));
        creds.add(new Credential(12223, "www.reddit.com", "leo"));
        creds.add(new Credential(242, "www.hacksor.com", "marc"));
        creds.add(new Credential(234, "www.yoak.com", "lop"));
        creds.add(new Credential(33123, "www.yaik.com", "looo"));


        ListView lv = (ListView) findViewById(R.id.listview_credentials);
        lv.setAdapter(new ArrayAdapter<Credential>(getBaseContext(), R.layout.listitem_credential, creds) {
            ArrayList<Credential> m_creds = creds;
            Context m_context = CredentialsListActivity.this;

            @Override
            public int getCount() {
                return m_creds.size();
            }

            @Override
            public Credential getItem(int i) {
                return m_creds.get(i);
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view==null){
                    // inflate the layout
                    LayoutInflater inflater = ((Activity) m_context).getLayoutInflater();
                    view = inflater.inflate(R.layout.listitem_credential, viewGroup, false);

                }

                ((TextView)view.findViewById(R.id.item_username)).setText(m_creds.get(i).username);
                ((TextView)view.findViewById(R.id.item_url)).setText(m_creds.get(i).url);

                return view;
            }



            @Override
            public boolean isEmpty() {
                return m_creds.isEmpty();
            }
        });
    }
}