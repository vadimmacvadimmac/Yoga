package info.mir.myapyoga;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;


public class MainActivity extends Activity {

    Button buttonConnect;  //для управления визуализацией кнопки
    Button buttonInfo;  //для управления визуализацией кнопки

    public final static String FILE_NAME = "filename_";

    static LoadThread mThing;  //поток загрузки
    static String url=null;               //для загрузки с ресурса
    static String resultHttp=null;
    static int aCount=0;   //какое количество заказов мы прочитали с сервера

   private static final Gson GSON= new Gson();
   //для распарсивания информации с сервиса (получим в эту структуру)
    class  ConnectInfo{
        public String status;
        public String count;

        public ConnectInfo(String status, String count){
            this.status=status;
            this.count=count;
        }
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.tv);
        buttonConnect = (Button) findViewById(R.id.btnStart);
        buttonInfo = (Button) findViewById(R.id.btnStop);

        Intent intent = getIntent();

                startService(new Intent(this, MyService.class));   //запуск сервиса на старте

        //полученная информация из сообщения сервиса
        String fileName = intent.getStringExtra(FILE_NAME);
        if (!TextUtils.isEmpty(fileName)) {
            ConnectInfo connectInfo = GSON.fromJson(fileName,ConnectInfo.class);  //распарсили
            try {
                aCount = Integer.parseInt(connectInfo.count);
            } catch (Exception e) {
                aCount = 0;
            }
            tv.setText(" Всего "+aCount+" шт. заказов");
            buttonConnect.setEnabled(true);  //теперь эта кнопка вызывает браузер - управление магазином
            buttonConnect.setText("Обработать заказ");
            buttonInfo.setText("Следить дальше");
        }

        mThing = new LoadThread();	//Создание потока
        url="http://yoga.mir.samocvet.info/cgi-bin/sup/magazin_info.exe";
        mThing.start();					//Запуск потока



    }



    public void onClickStart(View v) {
        startService(new Intent(this, MyService.class));   //запуск сервиса по кнопке

        if (aCount==0) {
            buttonConnect.setEnabled(false);
            }
        else {
            //вызов браузера
            Intent intent;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://povar.yoga.mir.samocvet.info"));
            startActivity(intent);
            }
    }


    public void onClickStop(View v) {
        //распарсиваем информацию с сервиса
        ConnectInfo connectInfo = GSON.fromJson(MainActivity.resultHttp,ConnectInfo.class);
        //отображаем информацию - сколько заказов шт.
        TextView tv = (TextView) findViewById(R.id.tv);
              //tv.setText(resultHttp+" - "+FILE_NAME);
        tv.setText(" Всего "+connectInfo.count+" шт. заказов ");   //MainActivity.resultHttp

        MyService.statusUser=0;   //сообщаем сервису что нужно следить дальше

        //stopService(new Intent(this, MyService.class));    //по идее завершает сервис, но что то не работает
    }



    class LoadThread extends Thread
    {
        @Override
        public void run()	//Этот метод будет выполнен в побочном потоке
        {
            String res="zagruzka";
            try {
                //URL url = new URL("http://example.com/?param=1");
                //URL url = new URL("https://www.alarstudios.com/test/auth.cgi?username="+loginConnect+"&password="+passwordConnect);
                URL url = new URL(MainActivity.url);
                //URL url = new URL("https://www.alarstudios.com/test/auth.cgi?username=test&password=123");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    int size= con.getContentLength();

                    for (int i=-1000; i<size; i++)
                    {
                        //res = in.readLine();
                        resultHttp=i+"_"+size;
                        i+=res.length();
                        //System.out.println(res);
                        //resultHttp=res;
                    }
                    res = in.readLine();
                    in.close();

                }
                else {
                    //System.out.println("Ошибка загрузки");

                    res="eror_line";
                }

            }catch(Exception e){res="erorE";}
            //System.out.println("Привет из побочного потока!");

            resultHttp=res;

        }
    }




}