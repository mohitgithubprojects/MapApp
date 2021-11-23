package com.example.mapboxapp

import android.Manifest
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapboxapp.databinding.ActivityMapsBinding
import com.example.mapboxapp.databinding.DroneDetailsPopupBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.PolylineOptions





class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private val droneMap: HashMap<String, MyLocation> = HashMap<String, MyLocation>()
    private val database = Firebase.database
    private var secondaryDatabase: FirebaseDatabase? = null
    var droneMarker: Marker? = null
    var polyline: Polyline? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fetchLocation()

        val options = FirebaseOptions.Builder()
            .setProjectId("dronepub")
            .setApplicationId("1:730993415286:android:930160843964ea34eb3b57")
            .setApiKey("AIzaSyCh8mtjKjlvu7RPRvchqyfGc4tpto0hMMY")
            .setDatabaseUrl("https://dronepub-default-rtdb.asia-southeast1.firebasedatabase.app/")
            // .setStorageBucket(...)
            .build()
        Firebase.initialize(this /* Context */, options, "secondary")

// Retrieve secondary FirebaseApp.
        val secondary = Firebase.app("secondary")
// Get the database for the other app.
        secondaryDatabase = Firebase.database(secondary)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnInfoWindowClickListener(OnInfoWindowClickListener { marker ->
            if (marker.title!! in droneMap.keys){
                droneMap[marker.title!!]?.let {
                    showDetailDialog(it)
                }
            }
        })
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
            return
        }
        mMap.isMyLocationEnabled = true
    }

    private fun showDetailDialog(myLocation: MyLocation) {
        val dialog = Dialog(this)
        val droneDetailsPopupBinding = DroneDetailsPopupBinding.inflate(layoutInflater)
        dialog.setContentView(droneDetailsPopupBinding.root)

        droneDetailsPopupBinding.droneUuidTv.text = myLocation.uuid
        droneDetailsPopupBinding.droneNameTv.text = myLocation.droneName
        droneDetailsPopupBinding.pilotNameTv.text = myLocation.pilotName
        droneDetailsPopupBinding.emailTv.text = myLocation.email
//        if (myLocation.rth == "true"){
//            droneDetailsPopupBinding.rthButton.setCardBackgroundColor(Color.LTGRAY)
//            droneDetailsPopupBinding.rthButton.isEnabled = false
//        }else{
//            droneDetailsPopupBinding.rthButton.setOnClickListener {
//                droneDetailsPopupBinding.rthButton.setCardBackgroundColor(Color.LTGRAY)
//                droneDetailsPopupBinding.rthButton.isEnabled = false
//                dialog.dismiss()
//                Toast.makeText(this, "${myLocation.droneName} is returning to launch station ....",Toast.LENGTH_SHORT).show()
//                if (myLocation.droneName == "drone1hex"){
//                    editRTHSecondary(myLocation.droneName)
//                }else{
//                    editRTH(myLocation.uuid)
//                }
//            }
//        }
        dialog.show()

    }

    private fun editRTH(uuid: String) {
        val myRef = database.getReference("users").child(uuid).ref
        myRef.child("rth").setValue("true").addOnFailureListener {
            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
        }
    }

    private fun editRTHSecondary(uuid: String) {
        val myRef = secondaryDatabase!!.getReference("server").child(uuid).ref
        myRef.child("command").child("RTL").setValue("true").addOnFailureListener {
            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
        }
    }

    private fun readFireBase(user: LatLng) {
        val myRef = database.getReference("users")
        val droneList = arrayListOf(
            "lnOjW9a61nOHI4LJdZMEgevmGRh1"
        )

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (drone in droneList) {
                    val lat = dataSnapshot.child(drone).child("data").child("phone")
                        .child("lat").value
                    val lon = dataSnapshot.child(drone).child("data").child("phone")
                        .child("lon").value
                    val droneName = drone
                    val uuid = dataSnapshot.child(drone).child("user").child("userId").value
                    val email = dataSnapshot.child(drone).child("user").child("email").value
                    val alt = dataSnapshot.child(drone).child("data").child("Altitude").value
                    val rth = dataSnapshot.child(drone).child("command").child("RTH").value
                    val myLocation = MyLocation(
                        uuid = uuid as String,
                        droneName = droneName as String,
                        email = email as String,
                        lat = lat as Double,
                        lon = lon as Double,
                        alt = alt as String,
                        rth = rth as String
                    )
                    droneMap.put(droneName as String, myLocation)

                    if (droneMarker == null){
                        droneMarker = mMap.addMarker(
                            MarkerOptions().position(LatLng(myLocation.lat, myLocation.lon)).title(droneName as String)
                                .icon(bitmapDescriptorFromVector(this@MapsActivity, R.drawable.ic_drone))
                        )
                    }else{
                        droneMarker!!.position = LatLng(myLocation.lat, myLocation.lon)
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(myLocation.lat, myLocation.lon), 15f))

//                    val marker = Marker(mMap, this@MapsActivity)
//                    marker.addMarker(LatLng(myLocation.lat, myLocation.lon), droneName as String)

                    //
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        myLocation.lat, myLocation.lon,
                        user.latitude, user.longitude,
                        results
                    )
                    binding.distanceTV.text = "Distance:  ${String.format("%.2f",results[0]/1000)} km"
                    val eta = formatTime(String.format("%.0f",results[0]/22.352).toLong())
                    binding.timeTV.text = "ETA:  $eta"

                    // Lines
                    if(results[0]<10){
                        binding.msgTV.text = "Your order has been reached near the drop location."
                    }
                    val options = PolylineOptions().width(10f).color(getColor(R.color.blue)).geodesic(true).jointType(JointType.ROUND)
                    options.add(user)
                    options.add(LatLng(myLocation.lat, myLocation.lon))
                    polyline?.remove()
                    polyline = mMap.addPolyline(options)

                }
                Log.d(TAG, "Value is: ${dataSnapshot.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
//        readSecondaryDrone()
    }

    private fun readSecondaryDrone(){
//        val ref = secondaryDatabase!!.getReference("server")
//        ref.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                val droneName = "drone1hex"
//                val lat = snapshot.child(droneName).child("data")
//                    .child("lat").value
//                val lon = snapshot.child(droneName).child("data")
//                    .child("lon").value
//                val uuid = "drone1hex"
//                val email = "NA"
//                val alt = snapshot.child(droneName).child("data")
//                    .child("alt").value
//                val rth = snapshot.child(droneName).child("command")
//                    .child("RTL").value
//
//                val myLocation = MyLocation(
//                    uuid = uuid as String,
//                    droneName = droneName as String,
//                    email = email as String,
//                    lat = lat as Double,
//                    lon = lon as Double,
//                    alt = alt as Double,
//                    rth = rth as String
//                )
//
//                droneMap.put(droneName as String, myLocation)
//                val marker = Marker(mMap, this@MapsActivity)
//                marker.addMarker(LatLng(myLocation.lat, myLocation.lon), droneName as String)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("MMM", error.message)
//            }
//        })
    }

    private fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                Log.d("mohit", "${it.latitude} && ${it.longitude} && ${it.altitude}")
                // Add a marker in Sydney and move the camera
                val user = LatLng(it.latitude, it.longitude)
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 15f))
                readFireBase(user)
            }
        }
    }

    fun formatTime(secs: Long): String {
        return String.format("%02d:%02d:%02d", secs / 3600, secs % 3600 / 60, secs % 60)
    }
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}