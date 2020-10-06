package com.vietis.atifsoftwares.model

class Post {
    private var postId: String = ""
    private var description: String = ""
    private var image: String = ""
    private var publisher: String = ""

    constructor()

    constructor(postId: String, description: String, image: String, publisher: String) {
        this.postId = postId
        this.description = description
        this.image = image
        this.publisher = publisher
    }

    fun getPostId(): String {
        return postId
    }

    fun setPostId(postId: String) {
        this.postId = postId
    }

    fun getDescription(): String {
        return description
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun getImage(): String {
        return image
    }

    fun setImage(image: String) {
        this.image = image
    }

    fun getPublisher(): String {
        return publisher
    }

    fun setPublisher(publisher: String) {
        this.publisher = publisher
    }
}