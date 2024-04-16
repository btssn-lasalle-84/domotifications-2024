package com.lasalle.domotifications;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;
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
    private static final String TAG                      = "_Communication"; //!< TAG pour les logs
    private static final String ADRESSE_STATION          = "station-lumineuse.local";
    private static final int    PORT_HTTP                = 80;
    public final static int CODE_HTTP_REPONSE_JSON = 0; //!< Code indicatif pour les message du Handler
    /**
     * Attributs
     */
    private static Communication communication =
      null; //!< Instance unique de Communication (singleton)
    private OkHttpClient       clientOkHttp = null;
    private String             adresseStation;
    private int                numeroPort                  = PORT_HTTP;
    private String             url                         = null;
    public static final String PREFERENCES                 = "preferences";
    public static final String PREFERENCES_ADRESSE_STATION = "adresseIPStation";
    SharedPreferences          preferences;

    private Communication(Context context)
    {
        this.clientOkHttp = new OkHttpClient();
        restaurerPreferences(context);
        // @todo Initialiser url sous la forme http://xxxx:80

        Log.d(TAG, "Communication() url = " + url);
    }

    private Communication(String adresseStation, Context context)
    {
        this.clientOkHttp = new OkHttpClient();
        restaurerPreferences(context);
        setAdresseStation(adresseStation);
        // @todo Initialiser url sous la forme http://xxxx:80/
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
            // @todo sauvegarder la valeur dans SharedPreferences
        }
    }

    public void emettreRequeteGET(String api, Handler handler)
    {
        if(clientOkHttp == null)
            return;
        // @todo si l'api ne contient pas le '/' l'ajouter au début

        // @todo créer l'url contenant l'api

        Log.d(TAG, "emettreRequeteGET() url = " + url);

        Request request =
          new Request.Builder().url(url).addHeader("Content-Type", "application/json").build();

        clientOkHttp.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {
                Log.d(TAG, "emettreRequeteGET() onFailure");
                e.printStackTrace();
                // @todo gérer une erreur de requête
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
                        // @todo créer un Message et ajouter le code indicatif dans what et la réponse dans obj

                        // @todo envoyer le Message avec sendMessage() du handler
                    }
                }.start();
            }
        });
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

    private void restaurerPreferences(Context context)
    {
        // @todo récupèrer le SharedPreferences à partir du contexte

        // @todo si l'élément existe ?
        if(preferences.contains(PREFERENCES_ADRESSE_STATION))
        {
            // @todo récupèrer l'adresse sauvegardée de la station

        }
        else
        {
            setAdresseStation(ADRESSE_STATION);
        }
    }
}
