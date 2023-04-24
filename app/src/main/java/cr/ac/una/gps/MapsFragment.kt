package cr.ac.una.gps


import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cr.ac.una.gps.adapter.UbicacionAdapter

import cr.ac.una.gps.dao.UbicacionDao
import cr.ac.una.gps.db.AppDatabase


import cr.ac.una.gps.entity.Ubicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MapsFragment : Fragment() {
    private lateinit var map: GoogleMap
    private var titulo: String = ""
    private lateinit var ubicacionDao: UbicacionDao
    private lateinit var locationReceiver: BroadcastReceiver
    //private lateinit var ubicaciones: List<Ubicacion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        ubicacionDao = AppDatabase.getInstance(requireContext()).ubicacionDao()
        titulo = prefs.getString("marker_label", "") ?: ""
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        iniciaServicio()//iniciaServicios es dar permisos

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        pintar()
        locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val latitud = intent?.getDoubleExtra("latitud", 0.0) ?: 0.0
                val longitud = intent?.getDoubleExtra("longitud", 0.0) ?: 0.0
                //println(latitud.toString() + "    " + longitud)
                val posicion = LatLng(latitud, longitud)
                val marcador = MarkerOptions().position(posicion)
                if (titulo.isNotEmpty()) {
                    marcador.title(titulo)
                }
                map.addMarker(marcador)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 12f))
                val entity = Ubicacion(null, latitud, longitud, Date())
                insertEntity(entity)
            }
        }
        context?.registerReceiver(locationReceiver, IntentFilter("ubicacionActualizada"))


    }

    fun pintar() {// es pintar todos los marcadores ya guardados en la base de datos
        Thread {
            val ubicaciones = ubicacionDao.getAll() as List<Ubicacion>
            //Para actualizar la interfaz de usuario con los resultados de la base de datos, se utiliza la función runOnUiThread() que permite ejecutar código en el hilo principal. Dentro de esta función, se itera sobre las ubicaciones y se agregan los marcadores al mapa.
            activity?.runOnUiThread {
                ubicaciones.forEach {
                    val miPosicion = LatLng(it.latitud, it.longitud)
                    val markerOptions = MarkerOptions().position(miPosicion)
                    if (titulo.isNotEmpty()) {
                        markerOptions.title(titulo)
                    }
                    map.addMarker(markerOptions)
                }
           }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        // Desregistrar el receptor al pausar el fragmento
        context?.unregisterReceiver(locationReceiver)
    }

    override fun onResume() {
        super.onResume()
        // Registrar el receptor para recibir actualizaciones de ubicación
        context?.registerReceiver(locationReceiver, IntentFilter("ubicacionActualizada"))
    }
//inserta a la base de datos
    private fun insertEntity(entity: Ubicacion) {
        //  private lateinit var ubicacionDao: UbicacionDAO
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ubicacionDao.insert(entity)
            }
        }
    }
//da permisos
    private fun iniciaServicio() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        } else {
            val intent = Intent(context, LocationService::class.java)
            context?.startService(intent)
        }
    }
}