package cr.ac.una.gps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import cr.ac.una.gps.dao.PoligonoDao
import cr.ac.una.gps.dao.UbicacionDao
import cr.ac.una.gps.db.AppDatabase
import cr.ac.una.gps.entity.Poligono
import cr.ac.una.gps.entity.Ubicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MapsFragment : Fragment() {

    private lateinit var map: GoogleMap
    private var titulo: String = ""
    private var telefono: String = ""
    private lateinit var ubicacionDao: UbicacionDao
    private lateinit var poligonoDao: PoligonoDao
    private lateinit var locationReceiver: BroadcastReceiver
    private lateinit var polygon: Polygon
    private lateinit var filtroPorFecha: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val preferenciasTelefono = requireActivity().getSharedPreferences("Mypreferencias", Context.MODE_PRIVATE)
        titulo = prefs.getString("marker_label", "") ?: ""
        telefono = preferenciasTelefono.getString("marker_label_telefono", "") ?: ""
        ubicacionDao = AppDatabase.getInstance(requireContext()).ubicacionDao()
        poligonoDao = AppDatabase.getInstance(requireContext()).poligonoDao()

    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        iniciaServicio()//iniciaServicios es dar permisos

    }

    private fun isLocationInsidePolygon(location: LatLng): Boolean {
        if (!::polygon.isInitialized) {
            return false
        }
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

        lifecycleScope.launch {
            polygon = createPolygon()
        }
        pintar()
        locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val latitud = intent?.getDoubleExtra("latitud", 0.0) ?: 0.0
                val longitud = intent?.getDoubleExtra("longitud", 0.0) ?: 0.0
                val posicion = LatLng(latitud, longitud)
                var marcador = MarkerOptions().position(posicion)
                if (isLocationInsidePolygon(posicion)){
                    makePhoneCall2()
                    marcador = MarkerOptions().position(posicion).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                }
                if (titulo.isNotEmpty()) {
                    marcador.title(titulo)
                }
                map.addMarker(marcador)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 8f))

                val entity = Ubicacion(null, latitud, longitud, Date(), isLocationInsidePolygon(posicion))
                insertEntity(entity)
            }
        }
        context?.registerReceiver(locationReceiver, IntentFilter("ubicacionActualizada"))

        filtroPorFecha = view.findViewById(R.id.filtroPorFecha)
        val btnActualizar = view.findViewById<Button>(R.id.btnActualizar)
        btnActualizar.setOnClickListener {
            try {
                val fechaTexto = filtroPorFecha.text.toString()
                val formato = SimpleDateFormat("MM/dd/yyyy")
                val fecha = formato.parse(fechaTexto)
                map.clear()
                pintarPorFecha(fecha)
                lifecycleScope.launch {
                    polygon = createPolygon()
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                // Manejo de excepciones, como mostrar un mensaje de error o registrar la excepción
            }
        }

    }

    private suspend fun createPolygon(): Polygon {
        val polygonOptions = PolygonOptions()
        val poligonos = withContext(Dispatchers.IO) {
            poligonoDao.getAll() as List<Poligono>
        }
        if (poligonos.size < 3) {

            polygonOptions.add(LatLng(10.630345,-84.8094284))
            polygonOptions.add(LatLng( 10.2954322,-85.1005661))
            polygonOptions.add(LatLng( 10.1710993,-84.4249069 ))
            polygonOptions.add(LatLng(  10.5385502,-84.161235 ))
            polygonOptions.add(LatLng( 10.7437006,-84.4633591 ))
            polygonOptions.add(LatLng(  10.630345,-84.8094284))
            /*
            polygonOptions.add(LatLng(-14.0095923,108.8152324))
            polygonOptions.add(LatLng( -43.3897529,104.2449199))
            polygonOptions.add(LatLng( -51.8906238,145.7292949))
            polygonOptions.add(LatLng( -31.7289525,163.3074199))
            polygonOptions.add(LatLng( -7.4505398,156.2761699))
            polygonOptions.add(LatLng( -14.0095923,108.8152324))
*/

            return map.addPolygon(polygonOptions)
        }
        poligonos.forEach { poligono ->
            val latitud = poligono.latitud
            val longitud = poligono.longitud
            polygonOptions.add(LatLng(latitud, longitud))
        }

        return map.addPolygon(polygonOptions)
    }
    fun pintarPorFecha(fechaFiltro: Date) {
        Thread {
            try {

                val ubicaciones = ubicacionDao.getAll() as List<Ubicacion>
                activity?.runOnUiThread {
                    ubicaciones.forEach {
                        val miPosicion = LatLng(it.latitud, it.longitud)
                        val markerOptions = MarkerOptions().position(miPosicion)

                        if (titulo.isNotEmpty()) {
                            markerOptions.title(titulo)
                        }
                        if (org.apache.commons.lang3.time.DateUtils.isSameDay(fechaFiltro, it.fecha)) {
                            println("entra")
                            map.addMarker(markerOptions)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Manejo de excepciones, como mostrar un mensaje de error o registrar la excepción
            }
        }.start()
    }

    /*fun pintarPorFecha(fechaFiltro : Date) {

        Thread {
            val ubicaciones = ubicacionDao.getAll() as List<Ubicacion>
            activity?.runOnUiThread {
                ubicaciones.forEach {
                    val miPosicion = LatLng(it.latitud, it.longitud)
                    val markerOptions = MarkerOptions().position(miPosicion)

                    if (titulo.isNotEmpty()) {
                        markerOptions.title(titulo)
                    }
                    if(org.apache.commons.lang3.time.DateUtils.isSameDay(fechaFiltro, it.fecha)) {
                        println("entra")
                        map.addMarker(markerOptions)
                    }
                }
            }
        }.start()
    }*/

    fun pintar() {// es pintar todos los marcadores ya guardados en la base de datos
        Thread {
            val ubicaciones =
                ubicacionDao.getAll() as List<Ubicacion> //Para actualizar la interfaz de usuario con los resultados de la base de datos, se utiliza la función runOnUiThread() que permite ejecutar código en el hilo principal. Dentro de esta función, se itera sobre las ubicaciones y se agregan los marcadores al mapa.
            activity?.runOnUiThread {
                ubicaciones.forEach {
                    val miPosicion = LatLng(it.latitud, it.longitud)
                    var markerOptions = MarkerOptions().position(miPosicion)

                    if (isLocationInsidePolygon(miPosicion)){
                        markerOptions = MarkerOptions().position(miPosicion).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    }
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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
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
// para las llamadas
    fun makePhoneCall3(){
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$telefono")
        startActivity(intent)
    }

    fun makePhoneCall2() {

        val permission = android.Manifest.permission.CALL_PHONE
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 1)
        } else {
            makePhoneCall3()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (1) {
            requestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall3()
                } else {
                    // El usuario no concedió el permiso
                }
            }
        }
    }

}
