package com.vietis.atifsoftwares.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vietis.atifsoftwares.R
import com.vietis.atifsoftwares.fragments.PostDetailsFragment
import com.vietis.atifsoftwares.fragments.ProfileFragment
import com.vietis.atifsoftwares.model.Notifications
import com.vietis.atifsoftwares.model.Post
import com.vietis.atifsoftwares.model.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class NotificationsAdapter(private val mContext: Context,
                           private val mNotifications: List<Notifications>) :
    RecyclerView.Adapter<NotificationsAdapter.NotificationsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_notifications_layout, parent, false)
        return NotificationsHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NotificationsHolder, position: Int) {
        val notification = mNotifications[position]
        when {
            notification.getText() == "Started following you" -> {
                holder.commentTv.text = "Started following you"
            }
            notification.getText() == "Likes your post" -> {
                holder.commentTv.text = "Likes your post"
            }
            notification.getText().contains("Commented:") -> {
                holder.commentTv.text = notification.getText()
            }
            else -> {
                holder.commentTv.text = notification.getText()
            }
        }
        userInfo(holder.imageProfile, holder.userNameTv, notification.getUserId())

        if (notification.isIsPost()) {
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.getPostId())
        } else {
            holder.postImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (notification.isIsPost()) {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("postId", notification.getPostId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PostDetailsFragment()).commit()
            } else {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("profileId", notification.getUserId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }
    }

    private fun userInfo(imageView: ImageView, userName: TextView, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(publisherId)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                            .into(imageView)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.profile).into(imageView)
                    }
                    userName.text = user!!.getUserName()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getPostImage(imageView: ImageView, postId: String) {
        val postsRef = FirebaseDatabase.getInstance().getReference("Posts")
            .child(postId)
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val post = snapshot.getValue(Post::class.java)
                    Picasso.get().load(post!!.getImage()).into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount(): Int {
        return mNotifications.size
    }

    inner class NotificationsHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageProfile: CircleImageView = itemView.findViewById(R.id.profile_image)
        val userNameTv: TextView = itemView.findViewById(R.id.user_name)
        val commentTv: TextView = itemView.findViewById(R.id.text_comment)
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
    }

}