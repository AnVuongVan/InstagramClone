package com.vietis.atifsoftwares

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.vietis.atifsoftwares.model.Story
import com.vietis.atifsoftwares.model.User
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    private var currentUserId: String = ""
    private var userId: String = ""
    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L

    private var imagesList: List<String>? = null
    private var storyIdsList: List<String>? = null
    private var storiesProgressView: StoriesProgressView? = null

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener{ _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener limit < (now - pressTime)
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId").toString()

        storiesProgressView = findViewById(R.id.stories_progress)
        layout_seen.visibility = View.GONE
        story_delete.visibility = View.GONE

        if (userId == currentUserId) {
            layout_seen.visibility = View.VISIBLE
            story_delete.visibility = View.VISIBLE
        }

        getStories(userId)
        userInfo(userId)

        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener { storiesProgressView!!.reverse() }
        reverse.setOnTouchListener(onTouchListener)

        val skip: View = findViewById(R.id.skip)
        skip.setOnClickListener { storiesProgressView!!.skip() }
        skip.setOnTouchListener(onTouchListener)

        seen_number.setOnClickListener {
            val intent = Intent(this, ShowUsersActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyId", storyIdsList!![counter])
            intent.putExtra("title", "Views")
            startActivity(intent)
        }

        story_delete.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId).child(storyIdsList!![counter])
            ref.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Story removed successfully", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getStories(userId: String) {
        imagesList = ArrayList()
        storyIdsList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Story").child(userId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (imagesList as ArrayList<String>).clear()
                (storyIdsList as ArrayList<String>).clear()

                for (dataSnapshot in snapshot.children) {
                    val story: Story? = dataSnapshot.getValue(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                        (imagesList as ArrayList<String>).add(story.getImageUrl())
                        (storyIdsList as ArrayList<String>).add(story.getStoryId())
                    }
                }
                storiesProgressView!!.setStoriesCount((imagesList as ArrayList<String>).size)
                storiesProgressView!!.setStoryDuration(5000L)
                storiesProgressView!!.setStoriesListener(this@StoryActivity)
                storiesProgressView!!.startStories(counter)
                Picasso.get().load(imagesList!![counter]).placeholder(R.drawable.profile).into(image_story)

                addViewToStory(storyIdsList!![counter])
                seenNumber(storyIdsList!![counter])
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun userInfo(userId: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    try {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                            .into(story_profile_image)
                    } catch (e: Exception) {
                        Picasso.get().load(R.drawable.profile).into(story_profile_image)
                    }
                    story_username.text = user!!.getUserName()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addViewToStory(storyId: String) {
        FirebaseDatabase.getInstance().getReference("Story")
            .child(userId).child(storyId).child("views")
            .child(currentUserId).setValue(true)
    }

    private fun seenNumber(storyId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Story")
            .child(userId).child(storyId).child("views")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                seen_number.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onNext() {
        Picasso.get().load(imagesList!![++counter]).placeholder(R.drawable.profile).into(image_story)
        addViewToStory(storyIdsList!![counter])
        seenNumber(storyIdsList!![counter])
    }

    override fun onPrev() {
        if (counter < 1) return
        Picasso.get().load(imagesList!![--counter]).placeholder(R.drawable.profile).into(image_story)
        seenNumber(storyIdsList!![counter])
    }

    override fun onComplete() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        storiesProgressView!!.destroy()
    }

    override fun onResume() {
        super.onResume()
        storiesProgressView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        storiesProgressView!!.pause()
    }

}