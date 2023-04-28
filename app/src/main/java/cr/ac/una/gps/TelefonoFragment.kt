package cr.ac.una.gps

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast



class TelefonoFragment : Fragment() {

    private lateinit var telefono: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_telefono, container, false)
        telefono = view.findViewById(R.id.telefono)
        val btnGuardaTelefono = view.findViewById<Button>(R.id.btn_guardar_telefono)

        btnGuardaTelefono.setOnClickListener {
            val label = telefono.text.toString()
            val preferencias = requireActivity().getSharedPreferences("Mypreferencias", Context.MODE_PRIVATE)
            val editarTelefono = preferencias.edit()
            editarTelefono.putString("marker_label_telefono", label)
            editarTelefono.apply()

            Toast.makeText(requireContext(), "Label saved", Toast.LENGTH_SHORT).show()
        }


        return view
    }
}