package cr.ac.una.gps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import cr.ac.una.gps.R
import cr.ac.una.gps.entity.Ubicacion

import kotlinx.coroutines.CoroutineScope

class UbicacionAdapter(context: Context, ubicaciones: List<Ubicacion>) :
    ArrayAdapter<Ubicacion>(context, 0, ubicaciones) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }

        val ubicacion = getItem(position)

        val fechaTextView = view!!.findViewById<TextView>(R.id.fecha)
        val latitudTextView = view.findViewById<TextView>(R.id.latitud)
        val longitudTextView = view.findViewById<TextView>(R.id.longitud)

        fechaTextView.text = ubicacion!!.fecha.toString()
        latitudTextView.text = ubicacion.latitud.toString()
        longitudTextView.text = ubicacion.longitud.toString()

        return view
    }
}