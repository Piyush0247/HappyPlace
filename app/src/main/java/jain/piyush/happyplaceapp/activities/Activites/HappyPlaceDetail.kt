package jain.piyush.happyplaceapp.activities.Activites

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import jain.piyush.happyplaceapp.R
import jain.piyush.happyplaceapp.activities.model.HappyPlaceModel


class HappyPlaceDetailActivity : AppCompatActivity() {
    private var toolbar_happy_place_detail : Toolbar = findViewById(R.id.toolbar_happy_place_detail)
    private var  iv_place_image : ImageView = findViewById(R.id.iv_place_image)
    private var  tv_description : TextView = findViewById(R.id.tv_description)
    private var  tv_location : TextView = findViewById(R.id.tv_location)

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_happy_place_detail)

        var happyPlaceDetailModel: HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            // get the Serializable data model class with the details in it
            happyPlaceDetailModel =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if (happyPlaceDetailModel != null) {

            setSupportActionBar(toolbar_happy_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title

            toolbar_happy_place_detail.setNavigationOnClickListener {
                onBackPressed()
            }

            iv_place_image.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            tv_description.text = happyPlaceDetailModel.description
            tv_location.text = happyPlaceDetailModel.location
        }
    }
}