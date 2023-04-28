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


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ConfiguracionFragment : Fragment() {

    private lateinit var etLabel: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_configuracion, container, false)
        etLabel = view.findViewById(R.id.et_label)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        btnSave.setOnClickListener {
            val label = etLabel.text.toString()
            val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("marker_label", label)
            editor.apply()
            Toast.makeText(requireContext(), "Label saved", Toast.LENGTH_SHORT).show()
        }
        return view
    }


}


