package com.vietis.atifsoftwares.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vietis.atifsoftwares.R
import com.vietis.atifsoftwares.fragments.ProfileFragment
import com.vietis.atifsoftwares.model.User
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private var mContext: Context,
                  private var mUser: List<User>,
                  private var isFragment: Boolean = false) :
    RecyclerView.Adapter<UserAdapter.UserHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_user_layout, parent, false)
        return UserHolder(view)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user = mUser[position]
        holder.userNameTv.text = user.getUserName()
        holder.fullNameTv.text = user.getFullName()
        try {
            Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.profileImage)
        } catch (e : Exception ) {
            Picasso.get().load(R.drawable.profile).into(holder.profileImage)
        }

        checkFollowingStatus(user.getUid(), holder.followBtn)

        holder.itemView.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.getUid())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.followBtn.setOnClickListener {
            if (holder.followBtn.text.toString() == "Follow") {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().getReference("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().getReference("Follow").child(user.getUid())
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(mContext, "Following", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }
                            }
                        }
                }
            } else {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().getReference("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().getReference("Follow").child(user.getUid())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(mContext, "UnFollow", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    private fun checkFollowingStatus(uid: String, followBtn: Button) {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().getReference("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.child(uid).exists()) {
                   followBtn.text = "Following"
               } else {
                   followBtn.text = "Follow"
               }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    inner class UserHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTv: TextView = itemView.findViewById(R.id.user_name)
        val fullNameTv: TextView = itemView.findViewById(R.id.full_name)
        val profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image)
        val followBtn: Button = itemView.findViewById(R.id.follow_btn)
    }

}