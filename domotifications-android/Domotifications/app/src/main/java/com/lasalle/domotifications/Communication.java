package com.lasalle.domotifications;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Context;
import static android.content.Context.CONNECTIVITY_SERVICE;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class Communication
{
    /**
     * Constantes
     */
    private static final String TAG                = "_Communication"; //!< TAG pour les logs
    private static final String ADRESSE_STATION    = "station-lumineuse.local";
    public static final String  ADRESSE_IP_STATION = "192.168.52.209"; // Simulateur station
    private static final int    PORT_HTTP          = 80;
    public final static int     CODE_HTTP_REPONSE_JSON =
      0; //!< Code indicatif de l'handler  pour les requêtes qui retournent des réponses au format
         //!< JSON
    public final static int CODE_HTTP_ERREUR =
      1; //!< Code indicatif de l'handler pour signaler des requêtes qui ont échouées (onFailure)
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    /**
     * Attributs
     */
    private static Communication communication =
      null; //!< Instance unique de Communication (singleton)
    private OkHttpClient  clientOkHttp = null;
    private BaseDeDonnees baseDeDonnees; //!< Association avec la base de donnees
    private String        adresseStation;
    private int           numeroPort = PORT_HTTP;
    private String        url        = null;

    private Communication(Context context)
    {
        this.clientOkHttp = new OkHttpClient();
        baseDeDonnees     = BaseDeDonnees.getInstance(context);
        url               = recupererURLStation();
        Log.d(TAG, "Communication() url = " + url);
    }

    private Communication(String adresseStation, Context context)
    {
        this.clientOkHttp = new OkHttpClient();
        baseDeDonnees     = BaseDeDonnees.getInstance(context);
        setAdresseStation(adresseStation);
        Log.d(TAG, "Communication() url = " + url);
    }

    public synchronized static Communication getInstance(Context context)
    {
        if(communication == null)
        {
            communication = new Communication(context);
        }
        return communication;
    }

    public synchronized static Communication getInstance(String adresseStation, Context context)
    {
        if(communication == null)
        {
            communication = new Communication(adresseStation, context);
        }
        return communication;
    }

    public String getAdresseStation()
    {
        return this.adresseStation;
    }

    public void setAdresseStation(String adresseStation)
    {
        if(adresseStation != this.adresseStation)
        {
            this.adresseStation = adresseStation;
            baseDeDonnees.sauvegarderURLServeurWeb(adresseStation);
        }
        url = "http://" + adresseStation + ":" + numeroPort;
    }

    public void emettreRequeteGET(String api, Handler handler)
    {
        if(clientOkHttp == null)
            return;

        if(!api.startsWith("/"))
        {
            api = "/" + api;
        }

        String urlRequete = url + api;

        Log.d(TAG, "emettreRequeteGET() url = " + urlRequete);

        Request request = new Request.Builder()
                            .url(urlRequete)
                            .addHeader("Content-Type", "application/json")
                            .build();

        clientOkHttp.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {
                Log.d(TAG, "emettreRequeteGET() onFailure");
                e.printStackTrace();
                Message message = Message.obtain();
                message.what    = CODE_HTTP_ERREUR;

                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                Log.d(TAG, "emettreRequeteGET() onResponse - message = " + response.message());
                Log.d(TAG, "emettreRequeteGET() onResponse - code    = " + response.code());

                if(!response.isSuccessful())
                {
                    throw new IOException(response.toString());
                }

                // la réponse à transmettre à l'emetteur de la requête
                final String body = response.body().string();
                Log.d(TAG, "emettreRequeteGET() onResponse - body = " + body);
                new Thread() {
                    @Override
                    public void run()
                    {
                        Message message = Message.obtain();
                        message.what    = CODE_HTTP_REPONSE_JSON;
                        message.obj     = body;
                        handler.sendMessage(message);
                    }
                }.start();
            }
        });
    }

    public void emettreRequetePATCH(String api, String json, Handler handler)
    {
        if(clientOkHttp == null)
            return;

        String urlRequete = url + api;

        Log.d(TAG, "emettreRequetePATCH() url  = " + urlRequete);
        Log.d(TAG, "emettreRequetePATCH() json = " + json);

        RequestBody body    = RequestBody.create(json, JSON);
        Request     request = new Request.Builder()
                            .url(urlRequete)
                            .addHeader("Content-Type", "application/json")
                            .patch(body)
                            .build();

        clientOkHttp.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {
                Log.d(TAG, "emettreRequetePATCH() onFailure");
                e.printStackTrace();
                handler.post(() -> {
                    Log.d(TAG, "Erreur lors de l'émission de la requête PATCH : " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                Log.d(TAG, "emettreRequetePATCH() onResponse - message = " + response.message());
                Log.d(TAG, "emettreRequetePATCH() onResponse - code    = " + response.code());
                handler.post(() -> {
                    if(response.isSuccessful()) {
                        Log.d(TAG, "La requête PATCH a été émise.");
                    } else {
                        Log.d(TAG, "La requête PATCH a échoué. Code d'erreur : " + response.code());
                    }
                });
            }
        });
    }

    private String recupererURLStation()
    {
        String urlServeurWeb = baseDeDonnees.getURLServeurWeb();

        return urlServeurWeb;
    }

    private boolean estConnecteReseau(Context context)
    {
        ConnectivityManager connectivityManager =
          (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnected() ||
           (networkInfo.getType() != ConnectivityManager.TYPE_WIFI &&
            networkInfo.getType() != ConnectivityManager.TYPE_MOBILE))
        {
            return false;
        }
        return true;
    }
}
