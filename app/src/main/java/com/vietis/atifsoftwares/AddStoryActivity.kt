package com.vietis.atifsoftwares

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.tasks.Continuation
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*

class AddStoryActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        storageRef = FirebaseStorage.getInstance().getReference("Stories Pictures")

        CropImage.activity().setAspectRatio(9, 16)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            uploadStory()
        }
    }

    private fun uploadStory() {
        when (imageUri) {
            null -> Toast.makeText(this, "Please select your picture", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding Story")
                progressDialog.setMessage("Please wait, we're adding your story")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()
                        val ref = FirebaseDatabase.getInstance().getReference("Story")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        val storyId = (ref.push().key).toString()
                        val timeEnd = System.currentTimeMillis() + 86400000

                        val storyMap = HashMap<String, Any>()
                        storyMap["userId"] = FirebaseAuth.getInstance().currentUser!!.uid
                        storyMap["timeStart"] = ServerValue.TIMESTAMP
                        storyMap["timeEnd"] = timeEnd
                        storyMap["imageUrl"] = myUrl
                        storyMap["storyId"] = storyId

                        ref.child(storyId).updateChildren(storyMap)
                        progressDialog.dismiss()
                        Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

}