package dk.fitfit.runtracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dk.fitfit.runtracker.BuildConfig
import dk.fitfit.runtracker.R
import kotlinx.android.synthetic.main.fragment_permission_request.*

private const val TAG = "PermissionRequestFrag"

/**
 * Displays information about why a user should enable either the fine location permission or the
 * background location permission (depending on what is needed).
 *
 * Allows users to grant the permissions as well.
 */
class PermissionRequestFragment : Fragment(R.layout.fragment_permission_request) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iconImageView.setImageResource(R.drawable.ic_location_on_24px)
        titleTextView.text = getString(R.string.fine_location_access_rationale_title_text)
        detailsTextView.text = getString(R.string.fine_location_access_rationale_details_text)
        permissionRequestButton.text = getString(R.string.enable_fine_location_button_text)

        permissionRequestButton.setOnClickListener {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
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
//                    Toast.makeText(context, "Permissions granted!", Toast.LENGTH_SHORT).show()

                else ->
                    Toast.makeText(context, "Permissions not granted!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    }
}
