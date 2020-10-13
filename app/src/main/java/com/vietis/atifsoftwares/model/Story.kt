package com.vietis.atifsoftwares.model

class Story {
    private var imageUrl: String = ""
    private var timeStart: Long = 0
    private var timeEnd: Long = 0
    private var storyId: String = ""
    private var userId: String = ""

    constructor()

    constructor(imageUrl: String, timeStart: Long, timeEnd: Long, storyId: String, userId: String) {
        this.imageUrl = imageUrl
        this.timeStart = timeStart
        this.timeEnd = timeEnd
        this.storyId = storyId
        this.userId = userId
    }

    fun getImageUrl(): String {
        return imageUrl
    }

    fun setImageUrl(imageUrl: String) {
        this.imageUrl = imageUrl
    }

    fun getTimeStart(): Long {
        return timeStart
    }

    fun setTimeStart(timeStart: Long) {
        this.timeStart = timeStart
    }

    fun getTimeEnd(): Long {
        return timeEnd
    }

    fun setTimeEnd(timeEnd: Long) {
        this.timeEnd = timeEnd
    }

    fun getStoryId(): String {
        return storyId
    }

    fun setStoryId(storyId: String) {
        this.storyId = storyId
    }

    fun getUserId(): String {
        return userId
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

}