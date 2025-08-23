package cat.tron.dictador.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cat.tron.dictador.R

class PortadaFragment : Fragment() {
   private lateinit var imatge: ImageView
   private lateinit var notaVersio: TextView

   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return inflater.inflate(R.layout.fragment_portada, container, false)
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)

      imatge = view.findViewById(R.id.img_microfon)
      notaVersio = view.findViewById(R.id.notaVersio)

      viewLifecycleOwner.lifecycleScope.launchWhenStarted {
         notaVersio.text = mostraVersio()
      }

      imatge.setOnClickListener {
         findNavController().navigate(R.id.action_PortadaFragment_to_DictatFragment)
      }
   }

   private fun mostraVersio(): String {
      return "${Build.MANUFACTURER} ${Build.MODEL}\n" +
            "ver. Android: ${Build.VERSION.RELEASE}"
   }
}
