package com.vietis.atifsoftwares

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storageRef = FirebaseStorage.getInstance().getReference("Posts Pictures")

        CropImage.activity().setAspectRatio(2, 1)
            .start(this)

        save_post_btn.setOnClickListener {
            uploadImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_post.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        when {
            imageUri == null -> Toast.makeText(this, "Please select your picture", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(descriptionEt.text.toString().trim()) -> {
                Toast.makeText(this, "Please enter your description", Toast.LENGTH_LONG).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, we're adding your post")
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
                        val ref = FirebaseDatabase.getInstance().getReference("Posts")
                        val postId = ref.push().key

                        val postMap = HashMap<String, Any>()
                        postMap["postId"] = postId!!
                        postMap["description"] = descriptionEt.text.toString().trim()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["image"] = myUrl

                        ref.child(postId).updateChildren(postMap)
                        progressDialog.dismiss()
                        Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_LONG).show()
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