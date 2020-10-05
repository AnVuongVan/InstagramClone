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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.vietis.atifsoftwares.model.User
import kotlinx.android.synthetic.main.activity_account_settings.*

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageRef = FirebaseStorage.getInstance().getReference("Profile Pictures")
        userInfo()

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image.setOnClickListener {
            checker = "clicked"
            CropImage.activity().setAspectRatio(1, 1)
                .start(this)
        }

        save_profile_btn.setOnClickListener {
            if (checker == "clicked") {
                uploadImage()
            } else {
                updateUserInfo()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_image.setImageURI(imageUri)
        }
    }

    private fun updateUserInfo() {
        when {
            TextUtils.isEmpty(full_name.text.toString().trim()) -> {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(user_name.text.toString().trim()) -> {
                Toast.makeText(this, "Please enter your user name", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(bio_profile.text.toString().trim()) -> {
                Toast.makeText(this, "Please enter your bio", Toast.LENGTH_LONG).show()
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().getReference("Users")
                val userMap = HashMap<String, Any>()
                userMap["fullName"] = full_name.text.toString().trim()
                userMap["userName"] = user_name.text.toString().trim()
                userMap["bio"] = bio_profile.text.toString().trim()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                            .into(profile_image)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.profile).into(profile_image)
                    }
                    user_name.setText(user?.getUserName())
                    full_name.setText(user?.getFullName())
                    bio_profile.setText(user?.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun uploadImage() {
        when {
            imageUri == null -> Toast.makeText(this, "Please select your picture", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(full_name.text.toString().trim()) -> {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(user_name.text.toString().trim()) -> {
                Toast.makeText(this, "Please enter your user name", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(bio_profile.text.toString().trim()) -> {
                Toast.makeText(this, "Please enter your bio", Toast.LENGTH_LONG).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Settings Account")
                progressDialog.setMessage("Please wait, we're updating your profile")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val fileRef = storageRef!!.child(firebaseUser.uid + ".jpg")
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
                        val ref = FirebaseDatabase.getInstance().getReference("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullName"] = full_name.text.toString().trim()
                        userMap["userName"] = user_name.text.toString().trim()
                        userMap["bio"] = bio_profile.text.toString().trim()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        progressDialog.dismiss()
                        Toast.makeText(this, "Account updated successfully", Toast.LENGTH_LONG)
                            .show()
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