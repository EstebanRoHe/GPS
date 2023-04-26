package cr.ac.una.gps


import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil

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
    private lateinit var polygon: Polygon

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

    private fun createPolygon(): Polygon {
        val polygonOptions = PolygonOptions()

       /* polygonOptions.add(LatLng(10.630345,-84.8094284))
        polygonOptions.add(LatLng( 10.2954322,-85.1005661))
        polygonOptions.add(LatLng( 10.1710993,-84.4249069 ))
        polygonOptions.add(LatLng(  10.5385502,-84.161235 ))
        polygonOptions.add(LatLng( 10.7437006,-84.4633591 ))
        polygonOptions.add(LatLng(  10.630345,-84.8094284))
*/
        polygonOptions.add(LatLng(-14.0095923,108.8152324))
        polygonOptions.add(LatLng( -43.3897529,104.2449199))
        polygonOptions.add(LatLng( -51.8906238,145.7292949))
        polygonOptions.add(LatLng( -31.7289525,163.3074199))
        polygonOptions.add(LatLng( -7.4505398,156.2761699))
        polygonOptions.add(LatLng( -14.0095923,108.8152324))

        return map.addPolygon(polygonOptions)

    }

    private fun isLocationInsidePolygon(location: LatLng): Boolean {
        return polygon != null && PolyUtil.containsLocation(location, polygon?.points, true)
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
                val posicion = LatLng(latitud, longitud)
                val marcador = MarkerOptions().position(posicion)
                if (titulo.isNotEmpty()) {
                    marcador.title(titulo)
                }

                polygon = createPolygon()
                if (isLocationInsidePolygon(posicion)){
                    println("+++++++++++++++++++++++++++++++++sidney esta en el mapa" )
                }

                if (!isLocationInsidePolygon(posicion)){
                    println("+++++++++++++++++++++++++++++++++CR no  esta en el mapa" )
                }

                map.addMarker(marcador)
               // map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 12f))

                val entity = Ubicacion(null, latitud, longitud, Date(), !isLocationInsidePolygon(posicion))
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