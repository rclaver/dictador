package cat.tron.dictador.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cat.tron.dictador.R
import cat.tron.dictador.databinding.FragmentPortadaBinding

class PortadaFragment : Fragment() {

   private var _binding: FragmentPortadaBinding? = null
   private val binding get() = _binding!!
   //private var idioma = "ca"

   private lateinit var microfon: ImageView
   private lateinit var audio: ImageView
   //private lateinit var radioGrupIdioma: RadioGroup
   private lateinit var notaVersio: TextView

   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      _binding = FragmentPortadaBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      initProperties()
/*
      radioGrupIdioma.setOnCheckedChangeListener { group, checkedId ->
         val radioBtn: RadioButton = view.findViewById(radioGrupIdioma.checkedRadioButtonId)
         idioma = radioBtn.text.toString().substring(0, 2).lowercase()
         Utilitats.canviaIdioma(idioma, requireContext())
         findNavController().navigate(R.id.action_PortadaFragment_to_DictatFragment)
      }
*/
      microfon.setOnClickListener {
         notaVersio.visibility = View.INVISIBLE
         //radioGrupIdioma.visibility = View.VISIBLE
         findNavController().navigate(R.id.action_PortadaFragment_to_DictatFragment)
      }

      audio.setOnClickListener {
         notaVersio.visibility = View.INVISIBLE
         //radioGrupIdioma.visibility = View.VISIBLE
         findNavController().navigate(R.id.action_PortadaFragment_to_AudioFragment)
      }

   }

   private fun initProperties() {
      //radioGrupIdioma = binding.radioGrupIdioma
      microfon = binding.imgMicrofon
      audio = binding.imgAudio
      notaVersio = binding.notaVersio
      notaVersio.text = mostraVersio()
   }

   private fun mostraVersio(): String {
      return "${Build.MANUFACTURER} ${Build.MODEL}\n" +
             "ver. Android: ${Build.VERSION.RELEASE}"
   }

}
