package com.example.exament2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Ciudad madrid=new Ciudad();
    private Ciudad barcelona=new Ciudad();
    private Ciudad sevilla=new Ciudad();
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://exament2-e728f-default-rtdb.firebaseio.com/");
    private DatabaseReference myRef = database.getReference("Ciudades");
    private ArrayList<Ciudad> ciudades = new ArrayList();
    private ArrayList<Ciudad> ciudadesFirebase=new ArrayList();
    Marker marcador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializarCiudades();
        hiloWebScrapping.start();
        try {
            Thread.sleep(3500);
            guardarCiudadesFirebase();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sacarCiudadesFirebase();



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void mostrarMarcadores() {
        for (Ciudad ciu:ciudadesFirebase
             ) {
            LatLng ciudadAct = new LatLng(Double.parseDouble(ciu.getLat()), Double.parseDouble(ciu.getLng()));
            Log.d("bien", ciu.getNombre());
            marcador = mMap.addMarker(new MarkerOptions().position(ciudadAct).title(ciu.getNombre()));
            marcador.setTag(ciu);

            if(ciu.getNombre().equals("Madrid")) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ciudadAct, 5));
            }
            mMap.setOnMarkerClickListener(this);
        }

    }

    private void sacarCiudadesFirebase() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Iterable<DataSnapshot> datos = dataSnapshot.getChildren();
                while (datos.iterator().hasNext()) {
                    DataSnapshot d = datos.iterator().next();
                    Ciudad cActual = d.getValue(Ciudad.class);
                    ciudadesFirebase.add(cActual);
                }
                //Debug Log.d("bien", ciudadesFirebase.size() + " ciudades sacadas");
                mostrarMarcadores();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Error", "Failed to read value.", error.toException());
            }
        });

    }

    private void guardarCiudadesFirebase() {
        for (Ciudad ciu:ciudades
             ) {
            myRef.push().setValue(ciu);
        }
    }

    private Thread hiloWebScrapping = new Thread() {
        public void run() {
            for (Ciudad ciu:ciudades
            ) {
                Document doc;
                try {
                    String url="https://es.wikipedia.org/wiki/"+ciu.getNombre();
                    //Debugg Log.d("bien", url);
                    doc = Jsoup.connect(url).get();
                    Elements latitude = doc.getElementsByClass("latitude");
                    Elements longitude = doc.getElementsByClass("longitude");
                    Elements foto = doc.getElementsByClass("image").select("img");
                    ciu.setUrl(foto.get(2).absUrl("src"));
                    String latitudee = latitude.get(0).text();
                    //Debugg Log.d("bien",latitudee);
                    String latitud = convertirCoord(latitudee);
                    String longitud = convertirCoord(longitude.get(0).text());
                    ciu.setLat(latitud);
                    ciu.setLng(longitud);
                    Log.d("bien",ciu.toString());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };


    private String convertirCoord(String text) {
        String coordenada="";
        ArrayList<String> coord = new ArrayList();
        String n="";

        for (int i=0;text.length()>i;i++) {
            try {
                int ncoord = Integer.parseInt(String.valueOf(text.charAt(i)));
                n += ncoord+"";
            } catch (Exception e) {
                if(n.length()>0)
                    coord.add(n);
                n = "";
            }
        }
        double coordfinal = Integer.parseInt(coord.get(0));
        coordfinal+=(Double.parseDouble(coord.get(1))/60);
        coordfinal+=(Double.parseDouble(coord.get(2))/3600);
        if (text.charAt(text.length()-1) == 'O') {
            coordfinal=coordfinal*-1;
        }
        Log.d("cord",coordfinal+"");
        coordenada+=coordfinal;
        return coordenada;
    }

    private void inicializarCiudades() {
        madrid.setNombre("Madrid");
        barcelona.setNombre("Barcelona");
        sevilla.setNombre("Sevilla");
        ciudades.add(madrid);
        ciudades.add(sevilla);
        ciudades.add(barcelona);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Ciudad ciudadclickada = (Ciudad) marker.getTag();
        crearAlertDialog(ciudadclickada);
        return false;
    }

    private void crearAlertDialog(Ciudad ciudad) {
        ImageView image = new ImageView(this);
        Picasso.get().load(ciudad.getUrl()).into(image);
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this).
                        setMessage("").
                        setCancelable(true).
                        setPositiveButton("enviar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).
                        setCancelable(true).
                        setPositiveButton("enviar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).
                        setPositiveButton("Cerrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).
                        setView(image);
        builder.create().show();
    }
}