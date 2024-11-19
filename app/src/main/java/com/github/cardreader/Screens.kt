package com.github.cardreader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.cardreader.sdk.reading.model.ReadData
import com.github.cardreader.ui.theme.LectureCarteBancaireTheme
import io.github.binaryfoo.DecodedData

@Composable
fun AskNewTag() {
    LectureCarteBancaireTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Approchez une carte du lecteur NFC",
                fontSize = 25.sp
            )
        }
    }
}

@Composable
fun TagDetected() {
    LectureCarteBancaireTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Carte détectée",
                fontSize = 25.sp
            )
        }
    }
}

@Composable
fun ReadInProgressScreen() {
    LectureCarteBancaireTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Lecture en cours",
                fontSize = 25.sp
            )
        }
    }
}

@Composable
fun ReadSuccessfulScreen(
    cardType: String = "cardType",
    pan: String = "123456789",
    cardData: ReadData,
    panToHide: String
) {
    LectureCarteBancaireTheme {
        Scaffold (
            modifier = Modifier.fillMaxSize(),
        ) {
            paddingValues ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Type : ")
                        }
                        append(cardType)
                    },
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("PAN : ")
                        }
                        append(pan)
                    },
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Informations issues du select PPSE",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                cardData.decodedPpse.forEach { data ->
                    ShowKids(data.kids, true, panToHide, pan)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Informations issues du GPO",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                cardData.decodedGpos.forEach { data ->
                    ShowKids(data.kids, true, panToHide, pan)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Informations issues des Read Records",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                cardData.decodedRecords.flatten().forEach { data ->
                    ShowKids(data.kids, true, panToHide, pan)
                }
            }
        }
    }
}

@Composable
fun ShowKids(kids: List<DecodedData>, showNested: Boolean, panToHide: String, panToReplaceWith: String) {
    Column {
        kids.forEach { data ->
            if (data.kids.isNotEmpty()) {

                HorizontalDivider(thickness = 2.dp, color = Color.Gray)
                Text(
                    text = data.rawData,
                    fontWeight = FontWeight.Bold
                )
                if (showNested) {
                    ShowKids(data.kids, true, panToHide, panToReplaceWith)
                } else {
                    Text("Information imbriquée cachée de taille " + data.kids.size)
                }
                HorizontalDivider(thickness = 2.dp, color = Color.Gray)

            } else {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = data.rawData,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = data.fullDecodedData.replace(panToHide, panToReplaceWith),
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (data.backgroundReading !== null && (data.backgroundReading?.get("short") != null || data.backgroundReading?.get(
                        "long"
                    ) != null)
                ) {
                    Text(
                        text = (data.backgroundReading?.get("short") + " " + data.backgroundReading?.get(
                            "long"
                        )),
                        fontSize = 14.sp
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ReadFailedScreen(
    cardType: String = "cardType",
    pan: String = "***",
    errorType: String = "error"
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    LectureCarteBancaireTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Type : ")
                    }

                    append(cardType)

                },
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("PAN : ")
                    }

                    append(pan)

                },
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Lecture échouée !",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = errorType,
                textAlign = TextAlign.Center,
            )
            Button(onClick = {
                val clipData = ClipData.newPlainText("label", errorType)
                clipboardManager.setPrimaryClip(clipData)
            }) {
                Text("Copier")
            }
        }
    }
}