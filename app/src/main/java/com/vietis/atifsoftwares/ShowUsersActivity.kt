package com.vietis.atifsoftwares

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vietis.atifsoftwares.adapter.UserAdapter
import com.vietis.atifsoftwares.model.User

class ShowUsersActivity : AppCompatActivity() {
    private var id: String = ""
    private var title: String = ""

    private var userAdapter: UserAdapter? = null
    private var userList: List<User>? = null
    private var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val intent = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()
        when (title) {
            "Likes" -> getLikes()
            "Following" -> getFollowing()
            "Followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getViews() {

    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().getReference("Follow")
            .child(id).child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (dataSnapshot in snapshot.children) {
                        (idList as ArrayList<String>).add(dataSnapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().getReference("Follow")
            .child(id).child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (dataSnapshot in snapshot.children) {
                        (idList as ArrayList<String>).add(dataSnapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(id)
        likesRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (dataSnapshot in snapshot.children) {
                        (idList as ArrayList<String>).add(dataSnapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showUsers() {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    for (id in idList!!) {
                        if (user!!.getUid() == id) {
                            (userList as ArrayList<User>).add(user)
                        }
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}