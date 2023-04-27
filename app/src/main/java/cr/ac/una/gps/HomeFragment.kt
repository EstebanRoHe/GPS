package cr.ac.una.gps

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.lifecycle.lifecycleScope
import cr.ac.una.gps.adapter.PoligonoAdapter
import cr.ac.una.gps.dao.PoligonoDao
import cr.ac.una.gps.db.AppDatabase
import cr.ac.una.gps.entity.Poligono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class HomeFragment : Fragment() {
    private lateinit var poligonoDao: PoligonoDao
    private lateinit var latitudPolygono: EditText
    private lateinit var longitudPolygono: EditText
    private lateinit var listView: ListView
    private lateinit var poligonos: List<Poligono>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        poligonoDao = AppDatabase.getInstance(requireContext()).poligonoDao()
    }

    private fun insertPoligono(entity: Poligono) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                poligonoDao.insert(entity)
                pintarPoligono()
            }
        }
    }

    private fun pintarPoligono() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                poligonos = poligonoDao.getAll() as List<Poligono>

            }
            requireActivity().runOnUiThread {
                val adapter = PoligonoAdapter(requireContext(), poligonos)
                listView.adapter = adapter
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        listView = view.findViewById(R.id.listPoligono)
        listView.isHorizontalScrollBarEnabled = true

        pintarPoligono()

        latitudPolygono = view.findViewById(R.id.latitudPolygono)
        longitudPolygono = view.findViewById(R.id.longitudPolygono)

        val btnGuardarPolygono = view.findViewById<Button>(R.id.btnGuardarPoligono)
        btnGuardarPolygono.setOnClickListener {
            val latitud = latitudPolygono.text.toString().toDouble()
            val longitud = longitudPolygono.text.toString().toDouble()
            val entity = Poligono(null, latitud, longitud)
            insertPoligono(entity)
        }

        return view
    }
}

