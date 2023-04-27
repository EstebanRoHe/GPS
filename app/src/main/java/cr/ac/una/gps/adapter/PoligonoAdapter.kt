package cr.ac.una.gps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import cr.ac.una.gps.R
import cr.ac.una.gps.entity.Poligono


class PoligonoAdapter (context: Context, poligonos: List<Poligono>) :
    ArrayAdapter<Poligono>(context, 0, poligonos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_poligono_item, parent, false)
        }

        val poligono = getItem(position)


        val latitudPoligoinoTextView = view!!.findViewById<TextView>(R.id.latitudPoligono)
        val longitudPoligoinoTextView = view.findViewById<TextView>(R.id.longitudPoligono)




        latitudPoligoinoTextView.text = poligono!!.latitud.toString()
        longitudPoligoinoTextView.text = poligono.longitud.toString()




        return view
    }
}





