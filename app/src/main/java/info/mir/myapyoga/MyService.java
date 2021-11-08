package info.mir.myapyoga;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import com.google.gson.Gson;


public class MyService extends Service {
    NotificationManager nm;


    static LoadThread mThing;  //поток загрузки
    static String url=null;               //для загрузки с ресурса
    static String resultHttp=null;
    static int statusLoad=0;
    static int statusUser=0;
    static int a=0;   //какое количество заказов мы прочитали с сервера

    private static final Gson GSON= new Gson();
    //для распарсивания информации с сервиса (получим в эту структуру)
    class  ConnectInfoS{
        public String status;
        public String count;

        public ConnectInfoS(String status, String count){
            this.status=status;
            this.count=count;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mThing = new MyService.LoadThread();	//Создание потока
        url="http://yoga.mir.samocvet.info/cgi-bin/sup/magazin_info.exe";   //.exe .! .asp
        mThing.start();					//Запуск потока загрузки с интернета

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //try {
        //    TimeUnit.SECONDS.sleep(5);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
       // }

        //resultHttp=resultHttp;
        new CountDownTimer(86400000,1000){
            @Override
            public void onTick(long t){
                //buttonConnect.setText(""+t/1000);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                 }

                switch (statusLoad){
                    case 0:{

                    }  break;
                    case 1:{
                        //if (statusUser==0) {
                            ConnectInfoS connectInfoS = GSON.fromJson(resultHttp, ConnectInfoS.class);
                            //int a;  //перенесли в главный класс
                            try {
                                a = Integer.parseInt(connectInfoS.count);
                            } catch (Exception e) {
                                a = 0;
                            }

                            if ((a > 0) & (statusUser!=a)) {
                                statusUser = a;
                                sendNotif();                    //активируем сообщение в телефоне
                            }
                            mThing = new MyService.LoadThread();	//повторное создание потока
                            url="http://yoga.mir.samocvet.info/cgi-bin/sup/magazin_info.exe";   //.exe .! .asp
                            MyService.mThing.start();                    //Запуск потока загрузки с интернета
                        //}
                    }  break;

                    default:{} break;
                }


            }
            @Override
            public void onFinish(){
                //-----
            }
        }.start();







        //сюда мы не добираемся из за таймера - это просто пример вызова сообщения
        //sendNotif();
        return super.onStartCommand(intent, flags, startId);
    }

    void sendNotif() {


        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.FILE_NAME, resultHttp);         //"somefile111"
        PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, 0);

        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)   //удаляет уведомление после активации
//                .setTicker("Text in status bar")     //на 7.0 невидно
                .setContentTitle("Новый заказ")    //"Notification`s title"
                .setContentText("Новых заказов - "+MyService.a)      //"Notification`s text"
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Color.RED)
                .setContentIntent(pIntent)
//                .setOngoing(true)   //нельзя смахнуть(отменить) уведомление
                .setSubText("Пора выполнять заказ")   //до API level 16,  в новых версиях этого нет //"This is subtext..."
                .setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "6")) //= Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
                .setUsesChronometer(true)
                .setNumber(2);   //просто цифра рядом счетчиком Chronometer, а мы тут будем ставить версию (видно в старых версиях до API level 16)
 //               .build();
        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

    }

    public IBinder onBind(Intent arg0) {
        return null;
    }



    class LoadThread extends Thread
    {
        @Override
        public void run()	//Этот метод будет выполнен в побочном потоке
        {
            statusLoad=0;
            String res="zagruzka";
            try {
                //URL url = new URL("http://example.com/?param=1");
                //URL url = new URL("https://www.alarstudios.com/test/auth.cgi?username="+loginConnect+"&password="+passwordConnect);
                URL url = new URL(MyService.url);
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
                statusLoad=1; //загрузка завершена
            }catch(Exception e){
                    res="erorE";
                    statusLoad=0;  //загрузка еще не завершилась, так как потерпела неудачу
                    }
            //System.out.println("Привет из побочного потока!");
            resultHttp=res;

        }
    }


}
