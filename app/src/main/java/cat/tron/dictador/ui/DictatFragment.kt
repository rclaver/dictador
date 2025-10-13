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
import cat.tron.dictador.R
import cat.tron.dictador.activitat.ProcesMicrofon
import cat.tron.dictador.activitat.Utilitats
import cat.tron.dictador.databinding.FragmentDictatBinding

class DictatFragment : Fragment() {
   private var _binding: FragmentDictatBinding? = null
   private val binding get() = _binding!!

   private lateinit var procesMicrofon: ProcesMicrofon
   private var estatIniciat: String? = null
   private var idioma = "ca"

   lateinit var avis: TextView
   lateinit var notes: TextView
   lateinit var lectura: TextView
   lateinit var error: TextView
   lateinit var btnInici: ImageView
   lateinit var btnPausa: ImageView
   lateinit var btnDesar: ImageView
   lateinit var btnStop: ImageView
   private lateinit var radioGrupIdioma: RadioGroup

   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      _binding = FragmentDictatBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      initProperties()

      avis.text = getString(R.string.inici_dictat)

      btnInici.setOnClickListener {
         val estat = estatIniciat ?: "primer_inici"
         if (estatIniciat == null) {
            procesMicrofon.setUp(binding, view.context.applicationContext)
         }
         estatIniciat = "inici"
         btnInici.visibility = View.INVISIBLE
         btnPausa.visibility = View.VISIBLE
         procesMicrofon.canviEstat(estat)
      }

      btnPausa.setOnClickListener {
         btnInici.visibility = View.VISIBLE
         btnPausa.visibility = View.INVISIBLE
         procesMicrofon.canviEstat("pausa")
      }

      btnDesar.setOnClickListener {
         btnInici.visibility = View.VISIBLE
         btnPausa.visibility = View.INVISIBLE
         procesMicrofon.canviEstat("desar")
      }

      btnStop.setOnClickListener {
         btnInici.visibility= View.VISIBLE
         btnPausa.visibility= View.INVISIBLE
         procesMicrofon.canviEstat("stop")
         //findNavController().navigate(R.id.action_DictatFragment_to_PortadaFragment)
      }

      radioGrupIdioma.setOnCheckedChangeListener { group, checkedId ->
         val radioBtn: RadioButton = view.findViewById(radioGrupIdioma.checkedRadioButtonId)
         idioma = radioBtn.text.toString().substring(0, 2).lowercase()
         Utilitats.canviaIdioma(idioma, requireContext())
         //findNavController().navigate(R.id.action_PortadaFragment_to_DictatFragment)
      }
   }

   private fun initProperties() {
      procesMicrofon = ProcesMicrofon()
      avis = binding.avis
      notes = binding.notes
      lectura = binding.lectura
      error = binding.error
      btnInici = binding.inici
      btnPausa = binding.pausa
      btnDesar = binding.desar
      btnStop = binding.stop
      radioGrupIdioma = binding.radioGrupIdioma

      btnInici.visibility= View.VISIBLE
      btnPausa.visibility= View.INVISIBLE
   }

   override fun onDestroyView() {
      super.onDestroyView()
      _binding = null
   }
}
