class Event(val id: Int, val x: Int, val y: Int, val isLongClick: Boolean) {
    operator fun component3(): Int = y
    operator fun component2() = x
    operator fun component4(): Boolean = isLongClick
    operator fun component1(): Int = id
}

fun isEventLongClicked(events: Array<Event>, eventId: Int): Boolean? {
    for ((id, _, _, isLongClick) in events) {
        if (id == eventId) return isLongClick
    }
    return null
}