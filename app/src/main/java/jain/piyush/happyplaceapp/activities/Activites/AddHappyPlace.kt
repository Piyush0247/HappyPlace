package jain.piyush.happyplaceapp.activities.Activites

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import jain.piyush.happyplaceapp.R
import jain.piyush.happyplaceapp.activities.database.DatabaseHandler
import jain.piyush.happyplaceapp.activities.model.HappyPlaceModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID


class AddHappyPlace : AppCompatActivity(), View.OnClickListener {
    private val  cal = Calendar.getInstance()
    private lateinit var dateSetListner : DatePickerDialog.OnDateSetListener
    private var savedImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mHappyPlaceDetail: HappyPlaceModel? = null
    private lateinit var name_of_place : EditText
    private lateinit var description_of_place : EditText
    private lateinit var date_of_place : EditText
    private lateinit var location_of_place : EditText
    private  lateinit var imageView : ImageView
    private  lateinit var add_image : TextView
    private lateinit var btnSaved : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)
        location_of_place = findViewById(R.id.location_of_place)
        date_of_place = findViewById(R.id.date_of_place)
        description_of_place  = findViewById(R.id.description_of_place)
        name_of_place = findViewById(R.id.name_of_place)
        imageView  = findViewById(R.id.image_view)
        add_image= findViewById(R.id.addImage)
        btnSaved= findViewById(R.id.btnSaved)
        val toolbar_AHP : Toolbar = findViewById(R.id.toolbar_AHP)

        setSupportActionBar(toolbar_AHP)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_AHP.setNavigationOnClickListener {
            onBackPressed()
        }
        if (!Places.isInitialized()){
            Places.initialize(this@AddHappyPlace,resources.getString(R.string.google_maps_api_key))
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetail = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }
        dateSetListner = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
           cal.set(Calendar.YEAR,year)
           cal.set(Calendar.MONTH,month)
           cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
        }
        updateDateFormat()
        if (mHappyPlaceDetail != null) {
            supportActionBar?.title = "Edit Happy Places"
            name_of_place.setText(mHappyPlaceDetail!!.title)
            description_of_place.setText(mHappyPlaceDetail!!.description)
            date_of_place.setText(mHappyPlaceDetail!!.date)
            location_of_place.setText(mHappyPlaceDetail!!.location)
            mLatitude = mHappyPlaceDetail!!.latitude
            mLongitude = mHappyPlaceDetail!!.longitude
            savedImageToInternalStorage = Uri.parse(mHappyPlaceDetail!!.image)
            imageView.setImageURI(savedImageToInternalStorage)
            btnSaved.text = "UPDATE"
        }

        date_of_place.setOnClickListener(this)
        add_image.setOnClickListener(this)
       btnSaved.setOnClickListener(this)
        location_of_place.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.date_of_place -> {
                    DatePickerDialog(
                        this@AddHappyPlace,
                        dateSetListner,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                R.id.addImage -> {
                    showPictureDialog()
                }
                R.id.btnSaved -> {
                    saveHappyPlace()
                }
                R.id.location_of_place ->{
                    locationFinder()
                }
            }
        }
    }

    private fun saveHappyPlace() {
        when {
            name_of_place.text.isNullOrEmpty() -> {
                showToast("Name should not be empty")
            }
            description_of_place.text.isNullOrEmpty() -> {
                showToast("Description should not be empty")
            }
            location_of_place.text.isNullOrEmpty() -> {
                showToast("Location should not be empty")
            }
            savedImageToInternalStorage == null -> {
                showToast("Please select an Image")
            }
            else -> {
                val happyPlaceModel = HappyPlaceModel(
                    if (mHappyPlaceDetail == null) 0 else mHappyPlaceDetail!!.id,
                    name_of_place.text.toString(),
                    savedImageToInternalStorage.toString(),
                    description_of_place.text.toString(),
                    date_of_place.text.toString(),
                    location_of_place.text.toString(),
                    mLatitude,
                    mLongitude
                )
                val dbHandler = DatabaseHandler(this@AddHappyPlace)
                val isInserted: Boolean = if (mHappyPlaceDetail == null) {
                    dbHandler.addHappyPlace(happyPlaceModel) > 0
                } else {
                    dbHandler.updateHappyPlace(happyPlaceModel) > 0
                }
                if (isInserted) {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    showToast("Failed to save Happy Place")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun locationFinder(){
        try {
            val fields = listOf(
                Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                .build(this@AddHappyPlace)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        // Here this is used to get an bitmap from URI
                        @Suppress("DEPRECATION")
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        savedImageToInternalStorage =
                            savedImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image : ", "Path :: $savedImageToInternalStorage")

                        imageView.setImageBitmap(selectedImageBitmap) // Set the selected image from GALLERY to imageView.
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else if (requestCode == CAMERA) {

                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap // Bitmap from camera

                savedImageToInternalStorage =
                    savedImageToInternalStorage(thumbnail)
                Log.e("Saved Image : ", "Path :: $savedImageToInternalStorage")

                imageView.setImageBitmap(thumbnail) // Set to the imageView.
            }
        } else if (resultCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            val place : Place = Autocomplete.getPlaceFromIntent(data!!)
            location_of_place.setText(place.address)
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
        }
    }

    private fun savedImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select")
        val pictureDialogItems = arrayOf("Select Photo from Gallery", "Capture Photo")
        pictureDialog.setItems(pictureDialogItems) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showRationalDialogForPermission()
                }
            }).onSameThread().check()
    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermission()
                }
            }).onSameThread().check()
    }
    private fun updateDateFormat() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        date_of_place.setText(sdf.format(cal.time))
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this).setMessage("It looks like you have turned off permission")
            .setPositiveButton("Go To Settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent) // Start the intent
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlaceImage"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}