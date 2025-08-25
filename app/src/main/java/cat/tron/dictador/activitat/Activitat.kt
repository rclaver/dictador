package cat.tron.dictador.activitat

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import cat.tron.dictador.R
import cat.tron.dictador.databinding.FragmentDictatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Activitat : AppCompatActivity() {
   private lateinit var ctxDictat: Context
   private lateinit var frgDictat: FragmentDictatBinding
   lateinit var cR: Resources

   private var titol = "pensaments"
   var nouText = ""
   private var estat = "inici"

   fun canviEstat(stat: String) {
      estat = stat
      if (estat == "primer_inici") {
         iniciDictat()
      }
   }

   fun iniciDictat() {
      GestorDeVeu.objTTS.inici()
      CoroutineScope(Dispatchers.Main).launch {
         withContext(Dispatchers.Main) {
            frgDictat.lectura.text = ""
         }
         nouText = processaEscena()
      }
      if (nouText.isNotEmpty()) {
         val errorTextView = frgDictat.error
         Utilitats.desaArxiu(titol, nouText, ctxDictat, errorTextView)
      }
   }

   private suspend fun processaEscena(): String {
      while (estat != "stop") {
         nouText = escoltaActor()
         withContext(Dispatchers.Main) {
            frgDictat.lectura.text = nouText
         }
         delay(50) //espera per donar temps a la UI
         while (estat == "pausa") {delay(50) } //esperar mentre estigui en pausa
      }
      return nouText
   }

   private suspend fun escoltaActor(): String {
      val text = GestorDeVeu.preparaReconeixementDeVeu(ctxDictat, frgDictat)
      if (text.isNotEmpty()) {
         mostraSentencia(text)
      } else {
         mostraError(cR.getString(R.string.error_no_escolto_res))
      }
      delay(100)
      return text
   }

   suspend fun mostraSentencia(text: String) {
      withContext(Dispatchers.Main) {
         frgDictat.lectura.text = text
         delay(100)
      }
   }

   private suspend fun mostraError(text: String) {
      withContext(Dispatchers.Main) {
         frgDictat.error.text = text
      }
   }

   fun setUp(fragmentDictat: FragmentDictatBinding, contextDictat: Context) {
      frgDictat = fragmentDictat
      ctxDictat = contextDictat
      cR = ctxDictat.resources
   }

}
