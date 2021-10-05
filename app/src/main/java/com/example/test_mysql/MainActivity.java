package com.example.test_mysql;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private TextView milkLocation;
    private EditText editText;
    private Button button;
    private String jsonString;
    ArrayList<Milk> milkArrayList;  //우유 정보를 저장할 ArrayList

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView=(TextView)findViewById(R.id.textView);
        milkLocation = (TextView)findViewById(R.id.milkLocation);
        editText=(EditText)findViewById(R.id.editText);
        button=(Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final JsonParse jsonParse = new JsonParse();  //AsyncTask 생성
                jsonParse.execute("http://192.168.35.77:80/select.php");   //AsyncTask 실행
            }
        });


    }

    public class JsonParse extends AsyncTask<String,Void,String> {
        String TAG = "JsonParseTest";
        @Override
        protected String doInBackground(String... strings) {    //execute의 매개변수를 받아와서 사용
            String url = strings[0];
            try {
                String selectData = "Data=" + editText.getText().toString();
                //따옴표 안과 php의 post[] 안의 이름이 같아야 함
                URL serverURL = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) serverURL.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(selectData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
                //어플에서 데이터 전송

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }   //연결상태 확인

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                Log.d(TAG, sb.toString().trim());

                return sb.toString().trim();    //받아온 JSON의 공백 제거

            } catch (Exception e) {
                Log.d(TAG, "InsertDat: Error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {    //s(doInBackgroundStirng)에서 return한 값을 받음
            super.onPostExecute(s);

            if(s==null)
                milkLocation.setText("error");
            else{
                jsonString=s;
                milkArrayList = doParse();
                if(milkArrayList.size()==0)  milkLocation.setText("검색결과 없음");
                //객체의 크기가 0인 경우 검색 결과가 없으므로 검색결과 없음 설정
                else {
                    String result="";
                    for(int i=0;i<milkArrayList.size();i++){
                        result += milkArrayList.get(i).getProduct_name()+"\n";
                    }
                    milkLocation.setText(result);

                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        private ArrayList<Milk> doParse(){
            ArrayList<Milk> tmpMilkArray = new ArrayList<Milk>();
            try{
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("carton");

                for(int i=0;i<jsonArray.length();i++){
                    Milk tmpMilk = new Milk();
                    JSONObject item = jsonArray.getJSONObject(i);
                    tmpMilk.setId(item.getString("id"));
                    tmpMilk.setProduct_name(item.getString("product_name"));
                    tmpMilkArray.add(tmpMilk);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            return tmpMilkArray;
        }//JSON을 가공하여 ArrayList에 넣음
    }

}