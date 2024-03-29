package com.vietis.atifsoftwares.model

class Notifications {
    private var userId: String = ""
    private var text: String = ""
    private var postId: String = ""
    private var isPost = false

    constructor()

    constructor(userId: String, text: String, postId: String, isPost: Boolean) {
        this.userId = userId
        this.text = text
        this.postId = postId
        this.isPost = isPost
    }

    fun getUserId(): String {
        return userId
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun getText(): String {
        return text
    }

    fun setText(text: String) {
        this.text = text
    }

    fun getPostId(): String {
        return postId
    }

    fun setPostId(postId: String) {
        this.postId = postId
    }

    fun isIsPost(): Boolean {
        return isPost
    }

    fun setIsPost(isPost: Boolean) {
        this.isPost = isPost
    }
}