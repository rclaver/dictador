package cat.tron.dictador.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cat.tron.dictador.R
import cat.tron.dictador.activitat.Utilitats
import cat.tron.dictador.databinding.FragmentConfiguracioBinding

class ConfiguracioFragment : Fragment() {

   private var _binding: FragmentConfiguracioBinding? = null
   private val binding get() = _binding!!

   private lateinit var botoDesar: Button
   private lateinit var selectorIdioma: Spinner
   private val opcionsIdioma = arrayOf("Català", "English", "Español")
   private var idiomaItemSelected = false

   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      _binding = FragmentConfiguracioBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      initProperties()

      viewLifecycleOwner.lifecycleScope.launchWhenStarted {
         if (!Utilitats.objEnFagmentSeleccio.get()) {
            Utilitats.canviaIdioma(Utilitats.objCompanyia.getIdioma(), requireContext())
            findNavController().navigate(R.id.action_ConfiguracioFragment_to_DictatFragment)
         } else {
            Utilitats.objEnFagmentSeleccio.set(false)
            creaFormulariConfiguracio(requireContext())
         }
      }

      selectorIdioma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
         override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (idiomaItemSelected) {
               idiomaItemSelected = false
               val idioma = selectorIdioma.selectedItem.toString().substring(0, 2).lowercase()
            }else {
               idiomaItemSelected = true
            }
         }
         override fun onNothingSelected(parent: AdapterView<*>) {}
      }

      botoDesar.setOnClickListener {
         val idioma = selectorIdioma.selectedItem.toString().substring(0,2).lowercase()
         Utilitats.canviaIdioma(idioma, requireContext())
         findNavController().navigate(R.id.action_ConfiguracioFragment_to_DictatFragment)
      }

   }

   /*
   Crea els elements del formulari, pel panell ConfiguracioFragment, per poder establir
   els paràmetres
   */
   private fun creaFormulariConfiguracio(context: Context) {
      // Establir opcions pel selector d'idioma
      var idiomes: Array<String> = arrayOf()
      opcionsIdioma.forEach { idiomes += it.substring(0,2).lowercase() }
      selectorIdioma.adapter = ArrayAdapter(context, R.layout.spinner, opcionsIdioma)
   }

   private fun initProperties() {
      selectorIdioma = binding.selectorIdioma
      botoDesar = binding.botoDesar
   }

   override fun onDestroyView() {
      super.onDestroyView()
      _binding = null
   }
}
