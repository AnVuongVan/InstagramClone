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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vietis.atifsoftwares.AddStoryActivity
import com.vietis.atifsoftwares.MainActivity
import com.vietis.atifsoftwares.R
import com.vietis.atifsoftwares.StoryActivity
import com.vietis.atifsoftwares.model.Story
import com.vietis.atifsoftwares.model.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.view.*

class StoryAdapter(private val mContext: Context,
                   private val mStory: List<Story>) :
    RecyclerView.Adapter<StoryAdapter.StoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.item_add_story, parent, false)
            StoryHolder(view)
        } else {
            val view = LayoutInflater.from(mContext).inflate(R.layout.item_story, parent, false)
            StoryHolder(view)
        }
    }

    override fun onBindViewHolder(holder: StoryHolder, position: Int) {
        val story = mStory[position]
        userInfo(holder, story.getUserId(), position)

        if (holder.adapterPosition != 0) {
            seenStory(holder, story.getUserId())
        }
        if (holder.adapterPosition == 0) {
            myStories(holder.storyTv!!, holder.storyPlus!!, false)
        }

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition == 0) {
                myStories(holder.storyTv!!, holder.storyPlus!!, true)
            } else {
                val intent = Intent(mContext, StoryActivity::class.java)
                intent.putExtra("userId", story.getUserId())
                mContext.startActivity(intent)
            }
        }
    }

    private fun userInfo(viewHolder: StoryHolder, userId: String, position: Int) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                            .into(viewHolder.storyImage)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.profile).into(viewHolder.storyImage)
                    }
                    if (position != 0) {
                        try {
                            Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                                .into(viewHolder.storySeen)
                        } catch (e: Exception) {
                            Picasso.get().load(R.drawable.profile).into(viewHolder.storySeen)
                        }
                        viewHolder.userNameTv!!.text = user!!.getUserName()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun myStories(textView: TextView, imageView: ImageView, click: Boolean) {
        val ref = FirebaseDatabase.getInstance().getReference("Story")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                var counter = 0
                val timeCurrent = System.currentTimeMillis()

                for (dataSnapshot in snapshot.children) {
                    val story = dataSnapshot.getValue(Story::class.java)
                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                        counter++
                    }
                }
                if (click) {
                    if (counter > 0) {
                        val alertDialog = AlertDialog.Builder(mContext).create()

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story") {
                                dialogInterface, _ ->
                            val intent = Intent(mContext, StoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialogInterface.dismiss()
                        }

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story") {
                                dialogInterface, _ ->
                            val intent = Intent(mContext, AddStoryActivity::class.java)
                            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialogInterface.dismiss()
                        }

                        alertDialog.show()
                    } else {
                        val intent = Intent(mContext, AddStoryActivity::class.java)
                        intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                        mContext.startActivity(intent)
                    }
                } else {
                    if (counter > 0) {
                        textView.text = "My Story"
                        imageView.visibility = View.GONE
                    } else {
                        textView.text = "Add Story"
                        imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun seenStory(viewHolder: StoryHolder, userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Story").child(userId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var i = 0;
                for (dataSnapshot in snapshot.children) {
                    if (!dataSnapshot.child("views").child(FirebaseAuth.getInstance().currentUser!!.uid).exists() &&
                            System.currentTimeMillis() < dataSnapshot.getValue(Story::class.java)!!.getTimeEnd()) {
                        i++
                    }
                }
                if (i > 0) {
                    viewHolder.storyImage!!.visibility = View.VISIBLE
                    viewHolder.storySeen!!.visibility = View.GONE
                } else {
                    viewHolder.storyImage!!.visibility = View.GONE
                    viewHolder.storySeen!!.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 0
        }
        return 1
    }

    inner class StoryHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        var storyImage: CircleImageView? = null
        var storySeen: CircleImageView? = null
        var userNameTv: TextView? = null
        var storyPlus: CircleImageView? = null
        var storyTv: TextView? = null

        init {
            storyImage = itemView.findViewById(R.id.story_image)
            storySeen = itemView.findViewById(R.id.story_seen)
            userNameTv = itemView.findViewById(R.id.story_username)
            storyPlus = itemView.findViewById(R.id.story_add)
            storyTv = itemView.findViewById(R.id.addStoryTv)
        }
    }

}