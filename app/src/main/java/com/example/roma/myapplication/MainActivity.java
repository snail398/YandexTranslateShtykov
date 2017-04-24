package com.example.roma.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringDef;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.*;
import android.database.Cursor;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Context context;

    FrameLayout root;
    private TextView translatedText;

    private TextView currenLangChooser;
    private Button swapLangButton;
    private TextView targetLangChooser;

    EditText inputTxt;
    boolean fromDb = false;

    CheckBox favCheckBox;
    boolean checkBoxState= false;

    private String currentLang ="en";
    private String targetLang ="ru";
    private String currentWord = "Insert text to translate here";
    private String currentTranslated = "";

    ListView favoriteTabsListView;
    ListView storyTabsListView;

    Display display;
    DisplayMetrics metricsB;

    private ControlSQL dbSQL;
    Cursor newtable;

    int step = 12;

    String temp;
    TranlatedWordAdapter trWordAdapter;
    TranslatedWord[] storyWords = null;
    TranslatedWord[] tempArr = null;

    private Timer mTimer;
    private TranslateTask translateTimerTask;

    String translForDB="";

    BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            root.removeAllViews();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    createMainTabs();
                    return true;
                case R.id.navigation_story:
                    storyTabsListView = createStoryTabs();
                    SendTableFromDBToListView(false,storyTabsListView);
                    return true;
                case R.id.navigation_favorite:
                    favoriteTabsListView = createFavoriteTabs();
                    SendTableFromDBToListView(true,favoriteTabsListView);
                    return true;
            }
            return false;
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void createMainTabs(){
        // создание выбора языка для ввода
        currenLangChooser = new TextView(context);
        FrameLayout.LayoutParams layPar3 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layPar3.topMargin = 0;
        layPar3.width = (int) (metricsB.widthPixels*0.35);
        layPar3.leftMargin=0;
        currenLangChooser.setLayoutParams(layPar3);
        currenLangChooser.setTextColor(Color.BLACK);
        currenLangChooser.setTextSize(30);
        currenLangChooser.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        currenLangChooser.setText(currentLang);
        currenLangChooser.invalidate();
        root.addView(currenLangChooser);


        // создание кнопки переключения языка
        swapLangButton = new Button(context);
        FrameLayout.LayoutParams layPar4 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layPar4.topMargin = 0;
        layPar4.width = (int) (metricsB.widthPixels*0.2);
        layPar4.leftMargin=(int) (metricsB.widthPixels*0.4);
        swapLangButton.setLayoutParams(layPar4);
        swapLangButton.invalidate();
        swapLangButton.setText("swap");
        swapLangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempLang;
                tempLang=currentLang;
                currentLang = targetLang;
                targetLang=tempLang;
                currenLangChooser.setText(currentLang);
                targetLangChooser.setText(targetLang);
                FullTranslate();
            }
        });
        root.addView(swapLangButton);

        //создание выбора целевого языка
        targetLangChooser = new TextView(context);
        FrameLayout.LayoutParams layPar5 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layPar5.topMargin = 0;
        layPar5.width = (int) (metricsB.widthPixels*0.4);
        layPar5.leftMargin=(int) (metricsB.widthPixels*0.65);
        targetLangChooser.setLayoutParams(layPar5);
        targetLangChooser.setTextColor(Color.BLACK);
        targetLangChooser.setTextSize(30);
        targetLangChooser.setText(targetLang);
        targetLangChooser.invalidate();
        root.addView(targetLangChooser);


        //Создание поля для ввода текста
        inputTxt = new EditText(context);
        FrameLayout.LayoutParams layPar = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layPar.topMargin = 200;
        inputTxt.setLayoutParams(layPar);
        inputTxt.setText(currentWord);
        inputTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (inputTxt.getText().toString().equals("Insert your text to translate here")){
                    inputTxt.setText("");
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentWord=inputTxt.getText().toString();
                //запуск таймера перевода. Переводить только когда пользователь не вводит текст 1.5 секунды
                if (mTimer!=null) {
                    mTimer.cancel();
                }
                mTimer = new Timer();
                translateTimerTask = new TranslateTask();
                mTimer.schedule(translateTimerTask, 1500);
                if (dbSQL.FindWordInDB(inputTxt.getText().toString()))
                    favCheckBox.setChecked(true);
                    else favCheckBox.setChecked(false);

            }
        });
        inputTxt.invalidate();
        root.addView(inputTxt);


        //создание отображения перевода
        translatedText = new TextView(context);
        FrameLayout.LayoutParams layPar2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layPar2.topMargin = 350;
        translatedText.setLayoutParams(layPar2);
        translatedText.setText(currentTranslated);
        translatedText.setTextColor(Color.BLACK);
        translatedText.setTextSize(20);
        translatedText.invalidate();
        root.addView(translatedText);

        // Чекбокс для избранного
        favCheckBox = new CheckBox(context);
        FrameLayout.LayoutParams layPar10 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layPar10.topMargin=320;
        layPar10.leftMargin = (int) (metricsB.widthPixels*0.7);
        favCheckBox.setLayoutParams(layPar10);
        favCheckBox.setText("Favorites");
        favCheckBox.setChecked(checkBoxState);
        favCheckBox.invalidate();
        favCheckBox.setVisibility(View.INVISIBLE);
        root.addView(favCheckBox);
        favCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    checkBoxState = true;
                    if(translForDB !="" && !dbSQL.FindWordInDB(inputTxt.getText().toString()))
                        dbSQL.createNewTable(inputTxt.getText().toString(),translForDB,"true");
                }
                else{
                    checkBoxState = false;
                    dbSQL.deleteOneTableFromFavorite(inputTxt.getText().toString());
                }
            }
        });
    }

    private ListView createStoryTabs(){

        final ListView list = new ListView(context);
        list.setClickable(true);
        list.invalidate();
        root.addView(list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromDb =true;
                navigation.setSelectedItemId(R.id.navigation_home);
                inputTxt.setText(tempArr[position].WORD);
            }
        });

        final Button btn = new Button(context);
        btn.setText("Clear");
        FrameLayout.LayoutParams layPar7 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layPar7.topMargin = 0;
        layPar7.width = (int) (metricsB.widthPixels*0.2);
        layPar7.leftMargin=(int) (metricsB.widthPixels-metricsB.widthPixels*0.2);
        btn.setLayoutParams(layPar7);
        btn.invalidate();
        root.addView(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // DeleteTableDB();
                dbSQL.deleteStory();
                trWordAdapter.notifyDataSetChanged();
            }
        });
        return list;
    }

    private ListView createFavoriteTabs(){
        final ListView favList = new ListView(context);
        favList.setClickable(true);
        favList.invalidate();
        root.addView(favList);
        favList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromDb = true;
                navigation.setSelectedItemId(R.id.navigation_home);
                inputTxt.setText(tempArr[position].WORD);
            }
        });

        final Button btn = new Button(context);
        btn.setText("Clear");
        FrameLayout.LayoutParams layPar7 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        layPar7.topMargin = 0;
        layPar7.width = (int) (metricsB.widthPixels*0.2);
        layPar7.leftMargin=(int) (metricsB.widthPixels-metricsB.widthPixels*0.2);
        btn.setLayoutParams(layPar7);
        btn.invalidate();
        root.addView(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DeleteTableDB();
                dbSQL.deleteFavorite();
                trWordAdapter.notifyDataSetChanged();
            }
        });
        return favList;
    }

    private void SendTableFromDBToListView(boolean isFavorite, ListView lst){
        dbSQL = new ControlSQL(context);

        if (isFavorite)
        newtable = dbSQL.getFavoriteTable();
        else newtable = dbSQL.getFullTable();

        if (newtable!=null){
            newtable.moveToFirst();
            storyWords = new TranslatedWord[newtable.getCount()];
            for (int i=0;i<storyWords.length;i++){
                int id = newtable.getInt(0);
                storyWords[i] = (new TranslatedWord(newtable.getString(1),newtable.getString(2)));
                newtable.moveToNext();
            }
            tempArr = new TranslatedWord[storyWords.length];
            for (int i=0;i<tempArr.length;i++) {
                tempArr[i] = storyWords[storyWords.length-i-1];
            }
            trWordAdapter = new TranlatedWordAdapter(context,R.layout.item, tempArr);
            lst.setAdapter(trWordAdapter);
        }
    }

    private  void DeleteTableDB(){
        dbSQL = new ControlSQL(context);
        newtable = dbSQL.getFullTable();
        if (newtable!=null){
            for (int i=0;i<5;i++){
                dbSQL.deleteTable(i);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context =(Context) this;

        display = getWindowManager().getDefaultDisplay();
        metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);

        root=(FrameLayout) findViewById(R.id.content);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);

        dbSQL = new ControlSQL(context);





    }

// перевод используя Яндекс.Переводчик
    private class translate extends AsyncTask<String,Integer,String> {
        String translated = "";
        String text ="";
        String lang ="";
        @Override
        protected String doInBackground(String... params) {
            translForDB = "";
            text = params[0];
            lang = params[1];
            try {
                URL url = new URL("https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170331T182937Z.e183b5fb235789e0.babd7984fdef8a25f5b49950c63a40272b39321b");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
                    dataOutputStream.writeBytes("text=" + URLEncoder.encode(params[0], "UTF-8") + "&lang=" + params[1]);
                    InputStream response = urlConnection.getInputStream();
                    String json = new java.util.Scanner(response).nextLine();
                    int start = json.indexOf("[");
                    int end = json.indexOf("]");
                    translated = json.substring(start + 2, end - 1);
                    translForDB = translated;
                } finally {
                    {
                        urlConnection.disconnect();
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }


            return translated;

        }
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            translatedText.setText("\n"+"1. "+translated);
            currentTranslated = "1. "+translated;
            if(translForDB !="" &&!fromDb) {
                dbSQL.createNewTable(inputTxt.getText().toString(),translForDB,"false");
            }
            fromDb = false;

        }
    }

    // перевод используя Яндекс.Словарь
    private class takeDictionary extends AsyncTask<String,Integer,String> {

        String tempJson ="";
        String out ="";
        @Override
        protected String doInBackground(String... params) {

            try {
                translForDB="";
                URL url = new URL("https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=dict.1.1.20170403T100953Z.78c55e2ba2470e3b.47f106ca57cc7d28b313e3fb5dadaad8db022d84");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try{
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    DataOutputStream dataOutputStream  = new DataOutputStream(urlConnection.getOutputStream());
                    dataOutputStream.writeBytes("&text="+ URLEncoder.encode(params[0],"UTF-8")+ "&lang="+params[1]);
                    InputStream response = urlConnection.getInputStream();
                    tempJson = new java.util.Scanner(response).nextLine();
                    JSONObject jsonobj = new JSONObject(tempJson);
                    // массив частей речи
                    JSONArray defArr = jsonobj.getJSONArray("def");
                    int count = 0;
                    for (int hi =0; hi<defArr.length();hi++)
                    {
                        //массив переводов
                        JSONArray trArray = defArr.getJSONObject(hi).getJSONArray("tr");

                        for (int j=0; j<trArray.length();j++){
                            count +=1;
                            out += "\n"+count+". "+trArray.getJSONObject(j).getString("text");
                            if (count ==1) translForDB = trArray.getJSONObject(j).getString("text");
                            JSONArray jsar = trArray.getJSONObject(j).names();
                            for (int z=0;z<jsar.length();z++){
                                //out +=  jsar.getString(z);
                                if (jsar.getString(z).equals("syn")){
                                    JSONArray tempArray =trArray.getJSONObject(j).getJSONArray("syn");
                                    for (int i =0; i<tempArray.length();i++) {
                                        out += ", " + tempArray.getJSONObject(i).getString("text");
                                    }
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    {
                        urlConnection.disconnect();
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            return out;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            translatedText.setText(out);
            currentTranslated = out;
            if(translForDB !="" && !fromDb) {
                dbSQL.createNewTable(inputTxt.getText().toString(),translForDB,"false");
            }
            fromDb = false;
            if (dbSQL.FindWordInDB(inputTxt.getText().toString())){
                favCheckBox.setChecked(true);
               // dbSQL.deleteLastTable();
            }
        }
    }
    //Функция, выбирающая чем переводить: Переводчиком или Словарем
    private void FullTranslate(){
        if (inputTxt.getText().toString().trim().split(" ").length>1)
            new translate().execute(inputTxt.getText().toString(),currentLang+"-"+targetLang);
        else {
            new takeDictionary().execute(inputTxt.getText().toString(),currentLang+"-"+targetLang);}
            favCheckBox.setVisibility(View.VISIBLE);
    }
    //Задание для таймера перевода
    class TranslateTask extends TimerTask{
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FullTranslate();

                }
            });
        }
    }
}
