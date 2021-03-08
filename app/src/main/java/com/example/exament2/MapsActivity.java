package com.example.exament2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Ciudad madrid=new Ciudad();
    private Ciudad barcelona=new Ciudad();
    private Ciudad sevilla=new Ciudad();
    private ArrayList<Ciudad> ciudades = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializarCiudades();
        sacarDatosCiudades();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void sacarDatosCiudades() {
        for (Ciudad ciu:ciudades
             ) {
            Document doc;
            try {
                String url="https://es.wikipedia.org/wiki/"+ciu.getNombre();
                Log.d("bien", url);
                doc = Jsoup.connect(url).get();
                Elements latitude = doc.getElementsByClass("latitude");
                Elements longitude = doc.getElementsByClass("longitude");
                Elements foto = doc.getElementsByClass("image");
                ciu.setUrl(foto.get(0).absUrl("src"));
                String latitud = convertirCoord(latitude.get(0).text());
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

    private String convertirCoord(String text) {
        String coordenada="";
        ArrayList<String> coord = new ArrayList();
        for (int i=0;text.length()>i;i++) {
            String n = "";
            try {
                int ncoord = Integer.parseInt(String.valueOf(text.charAt(i)));
                n += ncoord;
            } catch (Exception e) {
                coord.add(n);
                i++;
            }
        }
        int coordfinal = Integer.parseInt(coord.get(0));
        coordfinal+=(Integer.parseInt(coord.get(1))/60);
        coordfinal+=(Integer.parseInt(coord.get(2))/3600);
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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}