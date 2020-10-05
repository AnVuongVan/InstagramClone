package com.vietis.atifsoftwares.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vietis.atifsoftwares.AccountSettingsActivity
import com.vietis.atifsoftwares.R
import com.vietis.atifsoftwares.model.User
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        if (profileId == firebaseUser.uid) {
            view.edit_account_settings_btn.text = "Edit Profile"
        } else {
            checkFollowAndFollowing()
        }

        view.edit_account_settings_btn.setOnClickListener {
            when (view.edit_account_settings_btn.text.toString()) {
                "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                "Follow" -> {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().getReference("Follow").child(it1)
                            .child("Following").child(profileId).setValue(true)
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().getReference("Follow").child(profileId)
                            .child("Followers").child(it1).setValue(true)
                    }
                }

                "Following" -> {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().getReference("Follow").child(it1)
                            .child("Following").child(profileId).removeValue()
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().getReference("Follow").child(profileId)
                            .child("Followers").child(it1).removeValue()
                    }
                }
            }
        }

        getFollowers()
        getFollowing()
        userInfo()

        return view
    }

    private fun checkFollowAndFollowing() {
        val followingRef = firebaseUser.uid.let { it1 ->
            FirebaseDatabase.getInstance().getReference("Follow").child(it1)
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(profileId).exists()) {
                    view?.edit_account_settings_btn?.text = "Following"
                } else {
                    view?.edit_account_settings_btn?.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().getReference("Follow")
            .child(profileId).child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    view?.total_followers?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().getReference("Follow")
            .child(profileId).child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    view?.total_following?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(profileId)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                            .into(view?.profile_image)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.profile).into(view?.profile_image)
                    }
                    view?.profile_fragment_name?.text = user!!.getUserName()
                    view?.full_name_profile?.text = user.getFullName()
                    view?.bic_profile?.text = user.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref!!.putString("profileId", firebaseUser.uid)
        pref.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref!!.putString("profileId", firebaseUser.uid)
        pref.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref!!.putString("profileId", firebaseUser.uid)
        pref.apply()
    }

}