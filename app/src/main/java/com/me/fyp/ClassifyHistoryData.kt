package com.me.fyp

class ClassifyHistoryData {
    var imageUrl :String? = null
    var label :String? = null
    var confidence :String? = null
    var datetime :String? = null

    constructor(){}
    constructor(
            imageUrl :String?,
            label :String?,
            confidence :String?,
            datetime :String?
    ){
        this.imageUrl = imageUrl
        this.label = label
        this.confidence = confidence
        this.datetime = datetime
    }
}