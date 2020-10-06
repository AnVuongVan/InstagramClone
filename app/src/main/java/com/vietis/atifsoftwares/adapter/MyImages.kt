package com.vietis.atifsoftwares.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vietis.atifsoftwares.R
import com.vietis.atifsoftwares.fragments.PostDetailsFragment
import com.vietis.atifsoftwares.model.Post

class MyImages(private val mContext: Context,
               private val mPost: List<Post>) :
    RecyclerView.Adapter<MyImages.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_image_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mPost[position]
        Picasso.get().load(post.getImage()).into(holder.postImage)

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostId())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
    }

}