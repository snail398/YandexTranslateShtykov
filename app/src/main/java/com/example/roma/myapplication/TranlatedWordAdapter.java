package com.example.roma.myapplication;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lolol on 10.04.2017.
 */

public class TranlatedWordAdapter extends ArrayAdapter<TranslatedWord> {


    Context ctx;
   int layoutResourceId;
     TranslatedWord[] objects = null;
    ControlSQL dbSQL = null;

     TranlatedWordAdapter(Context context, int layoutResourseId, TranslatedWord[] words) {
         super(context,layoutResourseId,words);
         ctx = context;
         objects = words;
         layoutResourceId = layoutResourseId;
          dbSQL = new ControlSQL(ctx);
     }
    @Override
    public int getCount() {
        return objects.length;
    }

    @Override
    public TranslatedWord getItem(int position) {
        return objects[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view ==null) {
            LayoutInflater inflater = ((Activity)ctx).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);
        }

        TranslatedWord  trWord = objects[position];

        final TextView wrd = ((TextView) view.findViewById(R.id.WORD));
        final TextView trnsl =((TextView) view.findViewById(R.id.TRANSLATE));
        CheckBox box= ((CheckBox) view.findViewById(R.id.cbBox));
        box.setFocusable(false);
        wrd.setText(trWord.WORD);
        trnsl.setText(trWord.TRANSLATE);
        if (dbSQL.FindWordInDB(wrd.getText().toString())){
            box.setChecked(true);
        }
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                   //checkBoxState = true;
                        dbSQL.createNewTable(wrd.getText().toString(),trnsl.getText().toString(),"true");
                }
                else{
                   // checkBoxState = false;
                    dbSQL.deleteOneTableFromFavorite(wrd.getText().toString());
                }
            }
        });
        return view;
    }

}
