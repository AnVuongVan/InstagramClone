package com.vietis.atifsoftwares

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vietis.atifsoftwares.adapter.CommentAdapter
import com.vietis.atifsoftwares.model.Comment
import com.vietis.atifsoftwares.model.User
import kotlinx.android.synthetic.main.activity_account_settings.profile_image
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {
    private var postId = ""
    private var publisherId = ""
    private var commentAdapter: CommentAdapter? = null
    private var commentList: ArrayList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        val recyclerView: RecyclerView = findViewById(R.id.commentsRv)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentAdapter(this, commentList!!)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComments()
        getPostImage()

        back_btn.setOnClickListener {
            onBackPressed()
        }

        post_comment.setOnClickListener {
            if (TextUtils.isEmpty(add_comment.text.toString().trim())) {
                Toast.makeText(this, "Please enter your comment", Toast.LENGTH_LONG).show()
            } else {
                addComment()
            }
        }
    }

    private fun readComments() {
        val commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId)
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    commentList!!.clear()
                    for (dataSnapshot in snapshot.children) {
                        val model = dataSnapshot.getValue(Comment::class.java)
                        commentList!!.add(model!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().getReference("Comments")
            .child(postId)

        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = add_comment.text.toString().trim()
        commentsMap["publisher"] = publisherId

        commentsRef.push().setValue(commentsMap)
        add_comment.text.clear()
    }

    private fun getPostImage() {
        val postsRef = FirebaseDatabase.getInstance().getReference("Posts")
            .child(postId).child("image")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val postImage = snapshot.value.toString()
                    Picasso.get().load(postImage).into(post_image)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(publisherId)
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
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}