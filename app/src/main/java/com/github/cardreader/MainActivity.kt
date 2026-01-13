package com.github.cardreader

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentFilter.MalformedMimeTypeException
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.TagLostException
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.github.cardreader.chipreader.Constants
import com.github.cardreader.sdk.reading.CardReaderService
import com.github.cardreader.sdk.reading.model.Card
import com.github.cardreader.sdk.reading.model.DiscoverData
import com.github.cardreader.sdk.reading.model.ReadData
import com.github.cardreader.ui.theme.LectureCarteBancaireTheme
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LectureCarteBancaireTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndef.addDataType("*/*")
        } catch (e: MalformedMimeTypeException) {
            throw RuntimeException(e)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter!!.disableForegroundDispatch(this)
        NfcAdapter.getDefaultAdapter(this).disableReaderMode(this)
    }

    override fun onResume() {
        super.onResume()
        val options = Bundle()

        if (!nfcAdapter!!.isEnabled) {
            val alertbox = AlertDialog.Builder(this)
            alertbox.setTitle("NFC désactivé")
            alertbox.setMessage("Activez le NFC pour lire une carte")
            alertbox.setPositiveButton("Activer") { dialog, which ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                } else {
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
            }
            alertbox.show()
        }

        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 500)

        nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null)

        nfcAdapter!!.enableReaderMode(
            this, { tag: Tag? ->
                nfcAdapter!!.disableReaderMode(this@MainActivity)
                setContent {
                    TagDetected()
                }

                val vibrator =
                    this@MainActivity.getSystemService(VIBRATOR_SERVICE) as Vibrator

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            1000,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                }
            },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
            , options
        )
    }

    override fun onNewIntent(intent: Intent) {
        Log.d(Constants.LOG_TAG_DEMO, "New tag discovered")
        super.onNewIntent(intent)

        val tagFromIntent = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val terminal = CardReaderService(tagFromIntent)
        val discoverData: DiscoverData

        try {
            setContent {
                ReadInProgressScreen()
            }
            discoverData = terminal.firstContactWithNewCard()
        } catch (e: Exception) {
            when (e) {
                is TagLostException -> {
                    setContent {
                        AskNewTag()
                    }
                }

                else -> {
                    e.printStackTrace()
                    setContent {
                        ReadFailedScreen(
                            "Carte non supportée",
                            "Inconnu",
                            e.message!!
                        )
                    }
                }
            }
            return
        }
        val card: Card = discoverData.card()

        var pan = "****************"
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
            println("Handle $e in CoroutineExceptionHandler")
            when (e) {
                is TagLostException -> {
                    card.closeIsoDep()
                    setContent {
                        AskNewTag()
                    }
                }

                is IllegalStateException -> {
                    setContent {
                        ReadFailedScreen(
                            card.name,
                            formatPAN(pan),
                            "Le sans contact de la carte est désactivé ou non supporté"
                        )
                    }
                }

                else -> {
                    Log.d(Constants.LOG_TAG_DEMO, "Reading has failed", e)
                    setContent {
                        ReadFailedScreen(
                            card.name,
                            formatPAN(pan),
                            e.message!!
                        )
                    }
                }
            }
        }
        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val readData: ReadData = terminal.readCardAfl(discoverData)

            pan = readData.pan
            setContent {
                ReadSuccessfulScreen(
                    card.name,
                    formatPAN(pan),
                    readData,
                    pan
                )
            }

        }
    }

    private fun formatPAN(pan: String): String {
        return pan.substring(0, 4) + "********" + pan.substring(pan.length - 4)
        //return pan;
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Placez une carte sur le lecteur NFC",
            modifier = modifier,
            fontSize = 25.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LectureCarteBancaireTheme {
        MainScreen()
    }
}