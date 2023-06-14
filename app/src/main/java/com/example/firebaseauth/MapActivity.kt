package com.example.firebaseauth

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Polyline
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Math.*

@Suppress("DEPRECATION")
class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var timerTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var countUpTimer: CountUpTimer
    private lateinit var vremeView: View
    private var distanta_totala = 0
    private var ultima_locatie: Location? = null
    private var cursa_pornita = false
    private val listaPuncte = mutableListOf<LatLng>()
    private var polyline: Polyline? = null
    private var polyline2: PolylineOptions? = null
    private var locationCallback: LocationCallback? = null
    private lateinit var destinatie2: LatLng
    private lateinit var primaLocatie: LatLng
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var temperaturaTextView: TextView
    private lateinit var umiditateTextView: TextView
    private lateinit var puncte: List<com.google.android.gms.maps.model.LatLng>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.map_drawer)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        vremeView = findViewById(R.id.vreme_view)
        temperaturaTextView = findViewById(R.id.temperatura_textview)
        umiditateTextView = findViewById(R.id.umiditate_textview)
        distanceTextView = findViewById(R.id.distanta_valoare)
        vremeView.visibility = View.INVISIBLE
        temperaturaTextView.visibility = View.INVISIBLE
        umiditateTextView.visibility = View.INVISIBLE
        timerTextView = findViewById(R.id.timer_textview)
        val vitezaTextView = findViewById<TextView>(R.id.viteza_valoare)
        val butonStart = findViewById<Button>(R.id.start_button)
        val butonStop = findViewById<Button>(R.id.stop_button)
        val vitezaMedie = findViewById<TextView>(R.id.viteza_medie_valoare)
        val distanceTextView = findViewById<TextView>(R.id.distanta_valoare)
        var avgSpeedBD = 0
        var iteratii = 0
        var vitezaTotala = 0

        navView.setCheckedItem(R.id.nav_home)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.logout -> {
                    Firebase.auth.signOut()
                    val intent = Intent(this, SignUpActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.destinatie -> {
                    mapView.getMapAsync { googleMap ->
                        googleMap.setOnMapClickListener { latLng ->
                            val destinatie = LatLng(latLng.latitude, latLng.longitude)
                            polylineOptions = PolylineOptions()
                            googleMap.clear()
                            dateVreme(destinatie.latitude, destinatie.longitude)
                            sugereazaRute(primaLocatie, destinatie)
                        }
                    }
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }

        bottomNavigationView = findViewById(R.id.map_bot_navigation_menu)
        bottomNavigationView.clearFocus()
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_map -> {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                R.id.menu_history -> {
                    val intent = Intent(this, IstoricActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.trasee_salvate -> {
                    val intent = Intent(this, CurseSalvateActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView.getMapAsync { googleMap ->
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1
                )
                return@getMapAsync
            }
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val bundle = intent.getBundleExtra("bundle")
                val traseuLatLng = bundle?.getParcelableArrayList<LatLng>("traseu")
                if (location != null) {
                    var currentLatLng = LatLng(location.latitude, location.longitude)
                    primaLocatie = currentLatLng
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                    )
                    if (traseuLatLng != null) {
                        val distanta_bundle = bundle.getInt("distanta")
                        val viteza_bundle = bundle.getInt("viteza")
                        val durata_bundle = bundle.getString("durata")
                        distanceTextView.text = "$distanta_bundle m"
                        vitezaMedie.text = "$viteza_bundle KM/H"
                        timerTextView.text = "$durata_bundle"
                        polyline = googleMap.addPolyline(PolylineOptions().addAll(traseuLatLng))
                        val builder = LatLngBounds.builder()
                        for (point in polyline!!.points)
                            builder.include(point)
                        val bounds = builder.build()
                        val margin = 100
                        val cameraUpdate =
                            CameraUpdateFactory.newLatLngBounds(bounds, margin)
                        googleMap.animateCamera(cameraUpdate)
                    }
                }
            }
        }

        butonStart.setOnClickListener {
            cursa_pornita = true
            distanta_totala = 0
            distanceTextView.text = "0 m"
            vitezaTotala = 0
            polyline?.remove()
            listaPuncte.clear()
            startTimer()

            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        iteratii++
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        ValoareDistantaTotala(currentLatLng)
                        listaPuncte.add(currentLatLng)
                        val speed = location.speed
                        val vitezaKMH = (speed * 3.6).toInt() // conversie la km/h
                        vitezaTextView.text = "$vitezaKMH KM/H"
                        vitezaTotala += vitezaKMH
                        val vitezaMedieAfisata = vitezaTotala / iteratii
                        vitezaMedie.text = "$vitezaMedieAfisata KM/H"
                        avgSpeedBD = vitezaMedieAfisata
                    }
                    polyline2 = null
                    if (listaPuncte.isNotEmpty()) {
                        val polylineOptions = PolylineOptions()
                            .addAll(puncte)
                            .color(Color.RED)
                            .clickable(true)
                        mapView.getMapAsync { googleMap ->
                            googleMap.addPolyline(polylineOptions)
                        }
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        butonStop.setOnClickListener {
            if(cursa_pornita) {
                cursa_pornita = false
                fusedLocationClient.removeLocationUpdates(locationCallback)
                locationCallback = null
                countUpTimer.cancel()
                val mesajAlerta = AlertDialog.Builder(this)
                mesajAlerta.setTitle("Doriti sa salvati cursa?")
                mesajAlerta.setPositiveButton("Da") { _, _ ->
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val dataCurenta = Date()
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val data_format = dateFormat.format(dataCurenta)
                        val db = FirebaseFirestore.getInstance()
                        val docRef = db.collection("users").document(user.uid).collection("curse").document()
                        val data = hashMapOf(
                            "traseu" to listaPuncte.map { GeoPoint(it.latitude, it.longitude) },
                            "distanta" to distanta_totala,
                            "viteza medie" to avgSpeedBD,
                            "data" to data_format,
                            "durata" to timerTextView.text.toString()
                        )
                        docRef.set(data, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@MapActivity,
                                    "Cursa salvata cu succes!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@MapActivity,
                                    "Eroare: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        mapView.getMapAsync { googleMap ->
                            Toast.makeText(this@MapActivity, listaPuncte.toString(), Toast.LENGTH_SHORT).show()
                            polyline = googleMap.addPolyline(PolylineOptions().addAll(listaPuncte))
                            if (polyline?.points?.isNotEmpty() == true) {
                                val builder = LatLngBounds.builder()
                                for (point in polyline!!.points)
                                    builder.include(point)
                                val bounds = builder.build()
                                val margin = 100
                                val cameraUpdate =
                                    CameraUpdateFactory.newLatLngBounds(bounds, margin)
                                googleMap.animateCamera(cameraUpdate)
                            } else {
                                Toast.makeText(this@MapActivity, "polyline este gol nu inteleg nici eu cum...", Toast.LENGTH_SHORT).show()
                                Log.e("MapActivity", "Fara puncte in polyline :(")
                            }
                        }
                    }
                }
                mesajAlerta.setNegativeButton("Nu") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog = mesajAlerta.create()
                dialog.show()
            }
            else
                Toast.makeText(this@MapActivity, "Trebuie sa incepeti inregistrarea unei curse!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ValoareDistantaTotala(locationResult: LatLng?) {
        locationResult ?: return
        if (ultima_locatie != null) {
            val distance = FloatArray(1)
            Location.distanceBetween(ultima_locatie!!.latitude, ultima_locatie!!.longitude, locationResult.latitude, locationResult.longitude, distance)
            distanta_totala += (distance[0]).toInt()
            distanceTextView.text = "$distanta_totala m"
            }
        ultima_locatie = Location("")
        ultima_locatie!!.latitude = locationResult.latitude
        ultima_locatie!!.longitude = locationResult.longitude
    }

    private fun conversieLatLngs(latLngs: List<com.google.maps.model.LatLng>): List<com.google.android.gms.maps.model.LatLng> {
        val convertedLatLngs = mutableListOf<com.google.android.gms.maps.model.LatLng>()
        for (latLng in latLngs) {
            val convertedLatLng = com.google.android.gms.maps.model.LatLng(latLng.lat, latLng.lng)
            convertedLatLngs.add(convertedLatLng)
        }
        return convertedLatLngs
    }

    private fun sugereazaRute(origine: LatLng, destinatie: LatLng) {
        val apiKey = resources.getString(R.string.google_directions_api_key)
        val context = GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()

        val directionsApiRequest = DirectionsApi.newRequest(context)
            .mode(TravelMode.DRIVING)
            .origin(com.google.maps.model.LatLng(origine.latitude, origine.longitude))
            .destination(com.google.maps.model.LatLng(destinatie.latitude, destinatie.longitude))
            .alternatives(true)

        destinatie2 = destinatie

        val directionsResult = directionsApiRequest.await()
        val routes = directionsResult.routes
        val colors = generateRouteColors(routes.size)

        mapView.getMapAsync { googleMap ->
            googleMap.clear()
            for (i in routes.indices) {
                val route = routes[i]
                val points = conversieLatLngs(route.overviewPolyline.decodePath())
                val polylineOptions = PolylineOptions()
                    .addAll(points)
                    .color(colors[i])
                    .clickable(true)
                googleMap.addPolyline(polylineOptions)
                polyline2 = polylineOptions
                puncte = points
            }

            googleMap.setOnPolylineClickListener { polyline ->
                runOnUiThread {
                    val mesajTraseuSalvat = AlertDialog.Builder(this@MapActivity)
                    mesajTraseuSalvat.setTitle("Doriti sa salvati traseul pentru alta zi?")
                    mesajTraseuSalvat.setPositiveButton("Da") { _, _ ->

                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        val datePickerDialog = DatePickerDialog(
                            this@MapActivity,
                            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                                val selectedDate = Calendar.getInstance()
                                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                                val distanta = calculatePolylineDistance(polyline.points)
                                val formattedDay = String.format("%02d", selectedDay)
                                val formattedMonth = String.format("%02d", selectedMonth + 1)
                                val formattedYear = String.format("%04d", selectedYear)
                                val dataFormatted = "$formattedDay-$formattedMonth-$formattedYear"
                                val mesajAlerta = AlertDialog.Builder(this@MapActivity)
                                mesajAlerta.setTitle("Data selectata este ${dataFormatted}\nIntroduceti un titlu pentru traseu:")
                                val input = EditText(this)
                                mesajAlerta.setView(input)
                                mesajAlerta.setPositiveButton("OK") { _, _ ->
                                    val titlu = input.text.toString()
                                    if (titlu.isNotEmpty()) {
                                        val user = FirebaseAuth.getInstance().currentUser
                                        if (user != null) {
                                            val db = FirebaseFirestore.getInstance()
                                            val docRef =
                                                db.collection("users").document(user.uid)
                                                    .collection("trasee")
                                                    .document()
                                            val data = hashMapOf(
                                                "titlu" to titlu,
                                                "ruta" to polyline.points,
                                                "data" to dataFormatted,
                                                "distanta" to distanta
                                            )
                                            docRef.set(data, SetOptions.merge())
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        this@MapActivity,
                                                        "Traseu salvat cu succes!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        this@MapActivity,
                                                        "Eroare: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    } else {
                                        Toast.makeText(this, "Este necesara introducerea unui titlu", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                mesajAlerta.setNegativeButton("Anuleaza") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                val dialog = mesajAlerta.create()
                                dialog.show()
                            },
                            year,
                            month,
                            day
                        )
                        datePickerDialog.show()
                    }
                    mesajTraseuSalvat.setNegativeButton("Nu") {dialog, _ ->
                        dialog.dismiss()
                    }
                    val dialog1 = mesajTraseuSalvat.create()
                    dialog1.show()
                }
            }
        }
    }

    fun calculatePolylineDistance(points: List<LatLng>): Int {
        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            val startPoint = points[i]
            val endPoint = points[i + 1]
            val segmentDistance = calculateDistance(startPoint, endPoint)
            totalDistance += segmentDistance
        }
        return totalDistance.toInt()
    }

    fun calculateDistance(startPoint: LatLng, endPoint: LatLng): Int {
        val earthRadius = 6371000.0 // Radius of the Earth in meters
        val lat1 = startPoint.latitude
        val lon1 = startPoint.longitude
        val lat2 = endPoint.latitude
        val lon2 = endPoint.longitude

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c

        return distance.toInt()
    }

    private fun generateRouteColors(numRoutes: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val random = Random()

        for (i in 0 until numRoutes) {
            val r = random.nextInt(256)
            val g = random.nextInt(256)
            val b = random.nextInt(256)
            val color = Color.rgb(r, g, b)
            colors.add(color)
        }

        return colors
    }


    private fun dateVreme(latitude: Double, longitude: Double) {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=cf2cbe03431a9cea61ec2c040944e4e8"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(this@MapActivity, "Eroare la primirea datelor", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val weatherData = parcurgeDateJSON(responseBody)
                afisareDateVreme(weatherData)

                runOnUiThread {
                    vremeView.visibility = View.VISIBLE
                    val slideInRight = AnimationUtils.loadAnimation(applicationContext, R.anim.animatie)
                    vremeView.startAnimation(slideInRight)
                }

            }
        })
    }

    private fun parcurgeDateJSON(responseBody: String?): DateVremeAPI? {
        try {
            val jsonObject = JSONObject(responseBody)
            val temperature = jsonObject.getJSONObject("main").getDouble("temp")
            val humidity = jsonObject.getJSONObject("main").getInt("humidity")
            val weatherArray = jsonObject.getJSONArray("weather")
            val weatherJson = weatherArray.getJSONObject(0)
            val description = weatherJson.getString("description")

            return DateVremeAPI(temperature, humidity, description)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }

    private fun afisareDateVreme(dateVreme: DateVremeAPI?) {
        runOnUiThread {
            dateVreme?.let {
                temperaturaTextView.visibility = View.VISIBLE
                umiditateTextView.visibility = View.VISIBLE
                temperaturaTextView.text = "Temperatura: ${(it.temperature-273.15).toInt()}°C"
                umiditateTextView.text = "Umiditatea: ${it.humidity}%"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private inner class CountUpTimer : CountDownTimer(Long.MAX_VALUE, 1000) {
        private var elapsedSeconds = 0L - 1

        override fun onTick(millisUntilFinished: Long) {
            elapsedSeconds++
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val seconds = elapsedSeconds % 60
            val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            timerTextView.text = timeString
        }

        override fun onFinish() {

        }
    }

    private fun startTimer() {
        if (::countUpTimer.isInitialized) {
            countUpTimer.cancel()
        }
        countUpTimer = CountUpTimer()
        countUpTimer.start()
    }
}
