package cat.tron.dictador.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cat.tron.dictador.R
import cat.tron.dictador.activitat.ProcesAudio
import cat.tron.dictador.activitat.Utilitats
import cat.tron.dictador.databinding.FragmentAudioBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioFragment : Fragment() {
   private var _binding: FragmentAudioBinding? = null
   private val binding get() = _binding!!

   private lateinit var procesAudio: ProcesAudio
   private var idioma = "ca"

   lateinit var escriptura: TextView
   lateinit var error: TextView
   lateinit var btnInici: ImageView
   lateinit var btnDesar: ImageView
   lateinit var btnSortir: ImageView
   lateinit var radioGrupIdioma: RadioGroup

   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      _binding = FragmentAudioBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      initProperties()

      btnInici.setOnClickListener {
         procesAudio.iniciTranscripcio()
      }

      btnDesar.setOnClickListener {
         CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Main) { procesAudio.desaArxiu() }
         }
      }

      btnSortir.setOnClickListener {
         findNavController().navigate(R.id.action_AudioFragment_to_PortadaFragment)
      }

      radioGrupIdioma.setOnCheckedChangeListener { group, checkedId ->
         val radioBtn: RadioButton = view.findViewById(radioGrupIdioma.checkedRadioButtonId)
         idioma = radioBtn.text.toString().substring(0, 2).lowercase()
         Utilitats.canviaIdioma(idioma, requireContext())
         //findNavController().navigate(R.id.action_PortadaFragment_to_AudioFragment)
      }
   }

   private fun initProperties() {
      procesAudio = ProcesAudio()
      escriptura = binding.escriptura
      error = binding.error
      btnInici = binding.inici
      btnDesar = binding.desar
      btnSortir = binding.sortir
      radioGrupIdioma = binding.radioGrupIdioma
   }

   override fun onDestroyView() {
      super.onDestroyView()
      _binding = null
   }
}
