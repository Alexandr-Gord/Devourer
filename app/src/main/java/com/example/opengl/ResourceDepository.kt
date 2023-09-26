package com.example.opengl

import com.example.opengl.devourer.DevourerStructure
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ResourceDepository(private val dispatchPeriod: Int) {
    private val resourceQueue: BlockingQueue<Resource> = LinkedBlockingQueue()
    private var timer: Timer? = null
    fun takeResource(): Resource? {
        return try {
            resourceQueue.take()
        } catch (e: InterruptedException) {
            null
        }
    }

    fun addResource(resource: Resource) {
        resourceQueue.add(resource)
    }

    val size: Int
        get() = resourceQueue.size

    fun startResourceMining(
        indX: Int,
        indY: Int,
        devourerStructure: DevourerStructure?,
        startPosition: Position?,
        movingResources: MutableList<Resource?>
    ) {
        if (size > 0) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    val resource = takeResource()
                    if (resource == null) {
                        timer!!.cancel()
                        return
                    }
                    resource.startResource(
                        PositionInd(indX, indY),
                        startPosition,
                        devourerStructure
                    )
                    movingResources.add(resource)
                }
            }, 0, dispatchPeriod.toLong())
        }
    }
}