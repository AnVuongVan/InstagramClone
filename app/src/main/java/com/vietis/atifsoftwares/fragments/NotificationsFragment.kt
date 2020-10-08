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
import com.vietis.atifsoftwares.adapter.NotificationsAdapter
import com.vietis.atifsoftwares.model.Notifications
import kotlin.collections.ArrayList

class NotificationsFragment : Fragment() {
    private var notificationList: List<Notifications>? = null
    private var notificationsAdapter: NotificationsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        val recyclerView: RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_notifications)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        notificationList = ArrayList()
        notificationsAdapter = context?.let { NotificationsAdapter(it, notificationList as ArrayList<Notifications>) }
        recyclerView.adapter = notificationsAdapter

        readNotifications()
        return view
    }

    private fun readNotifications() {
        val ref = FirebaseDatabase.getInstance().getReference("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (notificationList as ArrayList<Notifications>).clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val notification = dataSnapshot.getValue(Notifications::class.java)
                        (notificationList as ArrayList<Notifications>).add(notification!!)
                    }
                    (notificationList as MutableList<*>).reverse()
                    notificationsAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}