package cr.ac.una.gps


import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import kotlin.system.exitProcess

class LocationService : Service() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Enviar la ubicación a través de un broadcast
                val intent = Intent("ubicacionActualizada")
                intent.putExtra("latitud", location.latitude)
                intent.putExtra("longitud", location.longitude)
                sendBroadcast(intent)
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Comenzar a recibir actualizaciones de ubicación cada 10 segundos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return super.onStartCommand(intent, flags, startId)
        }
        //da la actualizacion de lo que dura en actualizar el mapa
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0f, locationListener)
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener la actualización de la ubicación al destruir el servicio
        locationManager.removeUpdates(locationListener)
    }

    override fun onBind(intent: Intent?) = null



}