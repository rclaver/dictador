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
   private var estat = "inici"
   private var nouText = ""

   fun canviEstat(stat: String) {
      estat = stat
      if (estat == "primer_inici") {
         iniciDictat()
      }
   }

   fun iniciDictat() {
      //GestorDeVeu.objTTS.inici()
      CoroutineScope(Dispatchers.Main).launch {
         //withContext(Dispatchers.Main) { frgDictat.lectura.text = nouText }
         processaEscena()
         desaArxiu()
         delay(2000)
      }
   }

   private suspend fun processaEscena() {
      while (estat != "stop") {
         nouText += escoltaActor() + "\n"
         withContext(Dispatchers.Main) {
            frgDictat.lectura.text = nouText
         }
         delay(100) //espera per donar temps a la UI
         if (estat == "desar") {
            desaArxiu()
         }
         while (estat == "pausa") {delay(50) } //esperar mentre estigui en pausa
      }
   }

   private suspend fun escoltaActor(): String {
      val text = GestorDeVeu.preparaReconeixementDeVeu(ctxDictat, frgDictat)
      if (text.isNotEmpty()) {
         mostraSentencia(text)
      } else {
         mostraError(cR.getString(R.string.error_no_escolto_res))
      }
      delay(1000)
      return text
   }

   private suspend fun desaArxiu(): Boolean {
      val ret = nouText.isNotEmpty()
      if (ret) {
         val errorTextView = frgDictat.error
         if (Utilitats.desaArxiu(titol, nouText, ctxDictat, errorTextView)) {
            frgDictat.notes.text = cR.getString(R.string.text_desat)
         }
      }else {
         frgDictat.error.text = cR.getString(R.string.noutext_buit)
      }
      return ret
   }

   private suspend fun mostraSentencia(text: String) {
      withContext(Dispatchers.Main) {
         frgDictat.lectura.text = text
         delay(1000)
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
