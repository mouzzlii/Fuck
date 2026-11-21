
package de.trakuna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private val Forest = Color(0xFF174B3A)

private val carriers = listOf(
    "Österreichische Post",
    "DHL Paket",
    "DHL Express",
    "Deutsche Post",
    "DPD",
    "GLS",
    "UPS",
    "Hermes",
    "FedEx",
    "Sonstiger"
)

private val workflows = listOf(
    "Annahme",
    "Abholung",
    "Retoure",
    "Versand"
)

data class Parcel(
    val code: String,
    val carrier: String,
    val customer: String,
    val status: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences =
            getSharedPreferences("trakuna_data", MODE_PRIVATE)

        setContent {

            var parcels by remember {
                mutableStateOf(loadParcels(preferences.getString("parcels", "[]") ?: "[]"))
            }

            var selectedWorkflow by remember {
                mutableStateOf("Annahme")
            }

            var code by remember { mutableStateOf("") }
            var customer by remember { mutableStateOf("") }
            var search by remember { mutableStateOf("") }
            var carrier by remember {
                mutableStateOf(carriers.first())
            }

            var carrierMenu by remember { mutableStateOf(false) }
            var message by remember {
                mutableStateOf("Lokaler Testbetrieb")
            }

            fun saveParcels(updated: List<Parcel>) {
                parcels = updated

                val json = JSONArray()

                updated.forEach { parcel ->
                    json.put(
                        JSONObject()
                            .put("code", parcel.code)
                            .put("carrier", parcel.carrier)
                            .put("customer", parcel.customer)
                            .put("status", parcel.status)
                    )
                }

                preferences.edit()
                    .putString("parcels", json.toString())
                    .apply()
            }

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Forest,
                    background = Color(0xFFF3F6F3)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF3F6F3)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            "TRAKUNA",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Forest
                        )

                        Text(
                            "ALLE PAKETDIENSTE. EIN SMARTPHONE.",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "Paketshop-Ablauf",
                            style = MaterialTheme.typography.titleMedium
                        )

                        workflows.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { workflow ->
                                    Button(
                                        onClick = {
                                            selectedWorkflow = workflow
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor =
                                                if (selectedWorkflow == workflow)
                                                    Forest
                                                else Color.Gray
                                        )
                                    ) {
                                        Text(workflow)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Sendungsnummer / Barcode")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = customer,
                            onValueChange = { customer = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Kundenname (optional)") },
                            singleLine = true
                        )

                        Box {
                            OutlinedButton(
                                onClick = { carrierMenu = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Paketdienst: $carrier")
                            }

                            DropdownMenu(
                                expanded = carrierMenu,
                                onDismissRequest = {
                                    carrierMenu = false
                                }
                            ) {
                                carriers.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            carrier = item
                                            carrierMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val number = code.trim()

                                if (number.isBlank()) {
                                    message =
                                        "Bitte eine Sendungsnummer eingeben."
                                } else {
                                    val index =
                                        parcels.indexOfFirst {
                                            it.code == number
                                        }

                                    when (selectedWorkflow) {

                                        "Annahme" -> {
                                            if (index >= 0) {
                                                message =
                                                    "Diese Sendungsnummer existiert bereits."
                                            } else {
                                                saveParcels(
                                                    parcels + Parcel(
                                                        number,
                                                        carrier,
                                                        customer.trim(),
                                                        "Eingelagert"
                                                    )
                                                )
                                                message =
                                                    "Sendung lokal angenommen."
                                                code = ""
                                                customer = ""
                                            }
                                        }

                                        "Retoure" -> {
                                            saveParcels(
                                                parcels + Parcel(
                                                    number,
                                                    carrier,
                                                    customer.trim(),
                                                    "Retoure erfasst"
                                                )
                                            )
                                            message =
                                                "Retoure lokal erfasst."
                                            code = ""
                                            customer = ""
                                        }

                                        "Abholung" -> {
                                            if (index < 0) {
                                                message =
                                                    "Sendung wurde lokal nicht gefunden."
                                            } else {
                                                val updated =
                                                    parcels.toMutableList()
                                                updated[index] =
                                                    updated[index].copy(
                                                        status = "Abgeholt"
                                                    )
                                                saveParcels(updated)
                                                message =
                                                    "Abholung lokal gespeichert."
                                                code = ""
                                            }
                                        }

                                        "Versand" -> {
                                            if (index < 0) {
                                                message =
                                                    "Sendung wurde lokal nicht gefunden."
                                            } else {
                                                val updated =
                                                    parcels.toMutableList()
                                                updated[index] =
                                                    updated[index].copy(
                                                        status =
                                                            "Zur Übergabe vorgemerkt"
                                                    )
                                                saveParcels(updated)
                                                message =
                                                    "Übergabe lokal vorgemerkt."
                                                code = ""
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("$selectedWorkflow speichern")
                        }

                        HorizontalDivider()

                        Text(
                            "Sendungen (${parcels.size})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Sendungen durchsuchen") },
                            singleLine = true
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                parcels.filter {
                                    it.code.contains(search, true) ||
                                    it.customer.contains(search, true)
                                }
                            ) { parcel ->

                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            parcel.code,
                                            style =
                                                MaterialTheme.typography.titleMedium
                                        )
                                        Text(parcel.carrier)
                                        Text(parcel.customer)
                                        Text(
                                            parcel.status,
                                            color = Forest
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            "Hinweis: Lokaler Testbetrieb – keine echte Carrier-Buchung.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    private fun loadParcels(json: String): List<Parcel> {
        return try {
            val array = JSONArray(json)

            List(array.length()) { index ->
                val item = array.getJSONObject(index)

                Parcel(
                    code = item.getString("code"),
                    carrier = item.getString("carrier"),
                    customer = item.optString("customer"),
                    status = item.getString("status")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
