package dk.fitfit.runtracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dk.fitfit.runtracker.R
import kotlinx.android.synthetic.main.fragment_permission_request.*

private const val TAG = "PermissionRequestFrag"

class PermissionRequestFragment : Fragment(R.layout.fragment_permission_request) {
    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iconImageView.setImageResource(R.drawable.ic_location_on_24px)
        titleTextView.text = getString(R.string.fine_location_access_rationale_title_text)
        detailsTextView.text = getString(R.string.fine_location_access_rationale_details_text)
        permissionRequestButton.text = getString(R.string.enable_fine_location_button_text)

        permissionRequestButton.setOnClickListener {
            requestPermissions(permissions, REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionResult")

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() ->
                    Toast.makeText(context, "User interaction was cancelled.", Toast.LENGTH_SHORT).show()

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    findNavController().navigateUp()

                else ->
                    Toast.makeText(context, "Permissions not granted!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    }
}
