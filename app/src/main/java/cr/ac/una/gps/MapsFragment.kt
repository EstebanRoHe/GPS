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
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsFragment : Fragment() {
    private lateinit var map: GoogleMap
    private var titulo: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        titulo = prefs.getString("marker_label", "") ?: ""

    }
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        //Se dan los permisos

        if (ActivityCompat.checkSelfPermission(requireContext(),  android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(),  android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf( android.Manifest.permission.ACCESS_FINE_LOCATION,  android.Manifest.permission.ACCESS_COARSE_LOCATION),
                1)
            return@OnMapReadyCallback
        }
        // busca la posicion actual
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val miPosicion = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions().position(miPosicion)
                if (titulo.isNotEmpty()) {
                    markerOptions.title(titulo)
                }
                map.addMarker(markerOptions)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15f))
            }
        }

        map.setOnMapClickListener { latLng ->
            // Actualizar posición del marcador con un solo click
            val markerOptions = MarkerOptions().position(latLng)
            if (titulo.isNotEmpty()) {
                markerOptions.title(titulo)
            }
            map.clear()
            map.addMarker(markerOptions)

        }

        map.setOnMapLongClickListener {
                latLng ->
            // Actualizar posición del marcador con un long solo click
            val markerOptions = MarkerOptions().position(latLng)
            if (titulo.isNotEmpty()) {
                markerOptions.title(titulo)
            }
            map.clear()
            map.addMarker(markerOptions)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        //actualiza el fragment con la nueva pocision
        var actualButton = view.findViewById<Button>(R.id.btnActual)
        actualButton.setOnClickListener {
            map.clear()
            val mapNuevoFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapNuevoFragment?.getMapAsync(callback)
        }

    }


}



