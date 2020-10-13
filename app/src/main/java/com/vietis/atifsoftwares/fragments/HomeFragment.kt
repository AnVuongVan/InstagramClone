package com.vietis.atifsoftwares.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vietis.atifsoftwares.R
import com.vietis.atifsoftwares.adapter.PostAdapter
import com.vietis.atifsoftwares.adapter.StoryAdapter
import com.vietis.atifsoftwares.model.Post
import com.vietis.atifsoftwares.model.Story

class HomeFragment : Fragment() {
    private var postAdapter: PostAdapter? = null
    private var postList: ArrayList<Post>? = null
    private var followingList: ArrayList<String>? = null

    private var storyAdapter: StoryAdapter? = null
    private var storyList: ArrayList<Story>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView: RecyclerView?
        recyclerView = view.findViewById(R.id.recycler_view_home)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        var recyclerViewStory: RecyclerView? = null
        recyclerViewStory = view.findViewById(R.id.recycler_view_story)
        recyclerViewStory.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory.layoutManager = layoutManager

        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
        recyclerViewStory.adapter = storyAdapter

        checkFollowings()

        return view
    }

    private fun checkFollowings() {
        followingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().getReference("Follow")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    followingList?.clear()
                    for (dataSnapshot in snapshot.children) {
                        dataSnapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                    retrievePosts()
                    retrieveStories()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun retrieveStories() {
        val ref = FirebaseDatabase.getInstance().getReference("Story")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(Story("", 0, 0, "", FirebaseAuth.getInstance().currentUser!!.uid))
                for (id in followingList!!) {
                    var countStory = 0
                    var story: Story? = null
                    for (dataSnapshot in snapshot.child(id).children) {
                        story = dataSnapshot.getValue(Story::class.java)
                        if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().getReference("Posts")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()
                for (dataSnapshot in snapshot.children) {
                    val post = dataSnapshot.getValue(Post::class.java)
                    for (id in (followingList as ArrayList<*>)) {
                        if (post!!.getPublisher() == id) {
                            postList!!.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}