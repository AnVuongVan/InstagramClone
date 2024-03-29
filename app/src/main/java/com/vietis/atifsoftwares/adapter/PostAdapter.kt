package com.vietis.atifsoftwares.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.vietis.atifsoftwares.CommentsActivity
import com.vietis.atifsoftwares.MainActivity
import com.vietis.atifsoftwares.R
import com.vietis.atifsoftwares.ShowUsersActivity
import com.vietis.atifsoftwares.fragments.PostDetailsFragment
import com.vietis.atifsoftwares.fragments.ProfileFragment
import com.vietis.atifsoftwares.model.Post
import com.vietis.atifsoftwares.model.User
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext: Context,
                  private val mPost: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostHolder>() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return PostHolder(view)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]
        Picasso.get().load(post.getImage()).into(holder.postImage)
        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())

        if (post.getDescription() == "") {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.getDescription()
        }

        isLikes(post.getPostId(), holder.likesBtn)
        numberOfLikes(holder.likes, post.getPostId())
        numberOfComments(holder.comments, post.getPostId())
        checkSavedStatus(post.getPostId(), holder.saveBtn)

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostId())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }

        holder.publisher.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("profileId", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.profileImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("profileId", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.likesBtn.setOnClickListener {
            if (holder.likesBtn.tag == "Like") {
                FirebaseDatabase.getInstance().getReference("Likes")
                    .child(post.getPostId()).child(firebaseUser!!.uid).setValue(true)
                addNotifications(post.getPublisher(), post.getPostId())
            } else {
                FirebaseDatabase.getInstance().getReference("Likes")
                    .child(post.getPostId()).child(firebaseUser!!.uid).removeValue()
                mContext.startActivity(Intent(mContext, MainActivity::class.java))
            }
        }

        holder.commentBtn.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.getPostId())
            intent.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intent)
        }

        holder.comments.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.getPostId())
            intent.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intent)
        }

        holder.saveBtn.setOnClickListener {
            if (holder.saveBtn.tag == "Save") {
                FirebaseDatabase.getInstance().getReference("Saves")
                    .child(firebaseUser!!.uid).child(post.getPostId()).setValue(true)
            } else {
                FirebaseDatabase.getInstance().getReference("Saves")
                    .child(firebaseUser!!.uid).child(post.getPostId()).removeValue()
            }
        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", post.getPostId())
            intent.putExtra("title", "Likes")
            mContext.startActivity(intent)
        }
    }

    private fun addNotifications(userId: String, postId: String) {
        val notificationsRef = FirebaseDatabase.getInstance()
            .getReference("Notifications").child(userId)
        val hashMap = HashMap<String, Any>()
        hashMap["userId"] = firebaseUser!!.uid
        hashMap["text"] = "Likes your post"
        hashMap["postId"] = postId
        hashMap["isPost"] = true

        notificationsRef.push().setValue(hashMap)
    }

    private fun checkSavedStatus(postId: String, imageView: ImageView) {
        val savesRef = FirebaseDatabase.getInstance().getReference("Saves")
            .child(firebaseUser!!.uid)
        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(postId).exists()) {
                    imageView.setImageResource(R.drawable.ic_save_primary)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.ic_save_black)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun numberOfLikes(likes: TextView, postId: String) {
        val likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(postId)
        likesRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    likes.text = snapshot.childrenCount.toString() + " likes"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun numberOfComments(comments: TextView, postId: String) {
        val commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId)
        commentsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    comments.text = "View all " + snapshot.childrenCount.toString() + " comments"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun isLikes(postId: String, likesBtn: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(postId)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser!!.uid).exists()) {
                    likesBtn.setImageResource(R.drawable.ic_favorite_red)
                    likesBtn.tag = "Liked"
                } else {
                    likesBtn.setImageResource(R.drawable.ic_favorite_black)
                    likesBtn.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherStr: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(publisherStr)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).into(profileImage)
                    userName.text = user.getUserName()
                    publisher.text = user.getFullName()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    inner class PostHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image)
        val postImage: ImageView = itemView.findViewById(R.id.post_image_home)
        val likesBtn: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        val commentBtn: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        val saveBtn: ImageView = itemView.findViewById(R.id.post_save_btn)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val likes: TextView = itemView.findViewById(R.id.likes)
        val publisher: TextView = itemView.findViewById(R.id.publisher)
        val description: TextView = itemView.findViewById(R.id.description)
        val comments: TextView = itemView.findViewById(R.id.comments)
    }

}