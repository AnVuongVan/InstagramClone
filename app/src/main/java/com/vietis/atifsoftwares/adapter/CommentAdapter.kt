package com.vietis.atifsoftwares.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vietis.atifsoftwares.R
import com.vietis.atifsoftwares.model.Comment
import com.vietis.atifsoftwares.model.User
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class CommentAdapter(private val mContext: Context,
                     private val mComment: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_comment_layout, parent, false)
        return CommentHolder(view)
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val model = mComment[position]

        holder.commentTv.text = model.getComment()
        getUserInfo(holder.imageProfile, holder.userNameTv, model.getPublisher())
    }

    private fun getUserInfo(imageProfile: CircleImageView, userNameTv: TextView, publisher: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(publisher)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    userNameTv.text = user!!.getUserName()
                    try {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
                            .into(imageProfile)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.profile).into(imageProfile)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    inner class CommentHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageProfile: CircleImageView = itemView.findViewById(R.id.profile_image)
        val userNameTv: TextView = itemView.findViewById(R.id.user_name)
        val commentTv: TextView = itemView.findViewById(R.id.text_comment)
    }
}