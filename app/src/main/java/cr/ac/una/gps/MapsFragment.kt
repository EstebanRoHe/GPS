package cr.ac.una.gps
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsFragment : Fragment() {

    private lateinit var map: GoogleMap
    private var titulo: String = ""
    private lateinit var locationEditText : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        titulo = prefs.getString("marker_label", "") ?: ""
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        //Da permisos
        if (ActivityCompat.checkSelfPermission(requireContext(),  android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(),  android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf( android.Manifest.permission.ACCESS_FINE_LOCATION,  android.Manifest.permission.ACCESS_COARSE_LOCATION),
                1)
            return@OnMapReadyCallback
        }
        // Posicionar el marcador en la ubicación actual y le da el titulo al punto
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val miPosicion = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions().position(miPosicion)
                if (titulo.isNotEmpty()) {
                    markerOptions.title(titulo)
                }
                map.addMarker(markerOptions)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 0f))
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        //buscar por los edite text
        locationEditText = view.findViewById(R.id.editar_location_longitud)
        val latitudEditText = view.findViewById<EditText>(R.id.editar_location_latitud)
        val buscarButton = view.findViewById<Button>(R.id.btn_Buscar)
        buscarButton.setOnClickListener {
            val longitud = locationEditText.text.toString().toDoubleOrNull()
            val latitud = latitudEditText.text.toString().toDoubleOrNull()

            if (longitud != null && latitud != null) {
                val ubicacion = LatLng(latitud, longitud)
                map.addMarker(MarkerOptions().position(ubicacion).title(titulo))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 0f))
            } else {
                Toast.makeText(requireContext(), "Ingrese una ubicación válida", Toast.LENGTH_SHORT).show()
            }
        }


    }

}

/*
latitud, longitud
val sydney = LatLng(-34.0, 151.0)
            googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

 */





