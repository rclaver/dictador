package cat.tron.dictador.activitat

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
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
   private var enPausa = false
   private var stop = false
   private var pendentEscolta = false

   fun canviEstat(stat: String) {
      estat = stat
      enPausa = (estat == "pausa")
      stop = (estat == "stop")
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
         val escena = Utilitats.obraSencera("${titol}.txt")
         processaEscena(escena)
      }
   }

   private suspend fun processaEscena(fitxerEscena: DocumentFile? = null) {

      if (fitxerEscena?.exists() == true ) {
         val sentencies = Utilitats.llegeixArxiu(ctxDictat, fitxerEscena).split('\n')

         for (sentencia in sentencies) {
            var ret = ""
            var nar = ""
            if (sentencia.isNotEmpty()) {
               nar += processaFragment(sentencia)
               withContext(Dispatchers.Main) {
                  if (nar.isEmpty()) {
                     frgDictat.lectura.text = ret
                  }else {
                     frgDictat.narracio.text = nar
                  }
               }
               delay(50) //espera per donar temps a l'usuari (i a la UI)
            }
            if (stop) {
               break  //sortir del bucle de sentències d'aquesta escena
            }
            while (enPausa) {delay(50) } //esperar mentre estigui en pausa
         }
      }
   }

   private suspend fun processaFragment(text: String): String {
      var ret = ""
      pendentEscolta = true
      ret = mostraSentencia(text)
      if (pendentEscolta) {
         pendentEscolta = false
         frgDictat.lectura.text = text
         ret = escoltaActor(text)
      } else {
         delay(100)
      }
      return ret
   }

   suspend fun mostraSentencia(text: String): String {
      withContext(Dispatchers.Main) {
         frgDictat.lectura.text = text
         delay(100)
      }
      delay(100)
      return text
   }

   private suspend fun mostraError(text: String) {
      withContext(Dispatchers.Main) {
         frgDictat.error.text = text
      }
   }

   private suspend fun escoltaActor(text: String): String {
      val nouText = GestorDeVeu.preparaReconeixementDeVeu(ctxDictat, text, frgDictat)
      if (nouText.isEmpty()) {
         mostraError(cR.getString(R.string.error_no_escolto_res))
      }
      delay(100)
      return text
   }

   fun setUp(fragmentDictat: FragmentDictatBinding, contextDictat: Context) {
      frgDictat = fragmentDictat
      ctxDictat = contextDictat
      cR = ctxDictat.resources
   }

}
