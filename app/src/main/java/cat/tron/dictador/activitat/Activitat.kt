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
         withContext(Dispatchers.Main) {
            frgDictat.notes.text = cR.getString(R.string.text_desat)
            delay(10000)
         }
      }
   }

   private suspend fun processaEscena() {
      while (estat != "stop") {
         nouText += escoltaActor() + "\n"
         mostraTranscripcio(nouText)
         //withContext(Dispatchers.Main) { frgDictat.lectura.text = nouText }
         delay(100) //espera per donar temps a la UI
         if (estat == "desar") {
            if (desaArxiu()) {
               withContext(Dispatchers.Main) {
                  frgDictat.notes.text = cR.getString(R.string.text_desat)
                  delay(5000)
               }
            }
         }
         while (estat == "pausa") {delay(50) } //esperar mentre estigui en pausa
      }
   }

   private suspend fun escoltaActor(): String {
      val text = GestorDeVeu.preparaReconeixementDeVeu(ctxDictat, frgDictat)
      //if (text.isNotEmpty()) { mostraTranscripcio(text) } else { mostraError(cR.getString(R.string.error_no_escolto_res)) }
      delay(100)
      return text
   }

   private suspend fun desaArxiu(): Boolean {
      val ret = nouText.isNotEmpty()
      if (ret) {
         val errorTextView = frgDictat.error
         if (Utilitats.desaArxiu(titol, nouText, ctxDictat, errorTextView)) {
            withContext(Dispatchers.Main) {
               frgDictat.notes.text = cR.getString(R.string.text_desat)
            }
            delay(5000)
         }
      } else {
         mostraError(cR.getString(R.string.noutext_buit))
         delay(5000)
      }
      return ret
   }

   private suspend fun mostraTranscripcio(text: String) {
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
