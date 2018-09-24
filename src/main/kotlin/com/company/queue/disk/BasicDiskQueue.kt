package com.company.queue.disk

import com.company.queue.BasicMessage
import com.company.queue.base.*
import java.nio.file.Path


/**
 * Created by remote on 9/10/18.
 */
open class BasicDiskQueue(private val baseDir: Path,
                          batchSize: Int,
                          private val commonQueue: CommonQueue<BasicMessage> = BasicDiskQueue.build(baseDir, batchSize)) : BaseQueue<BasicMessage> {
    init {
        baseDir.toFile().mkdirs()
    }

    override fun load(): List<Node<BasicMessage>> = commonQueue.poll().map { it }.toList()

    override fun put(item: BasicMessage) = commonQueue.put(item)


    companion object {
        private val HEAD_ID = "0000-0000-0000-0000"
        private val TAIL_ID = "1111-1111-1111-1111"
        private val PROCESSED_ELEMENT_HEAD_ID = "2222-2222-2222-2222"
        private val DUMMY_ID = "3333-3333-3333-3333"


        private fun build(baseDir: Path, batchSize: Int): CommonQueue<BasicMessage> {
            return CommonQueue(newAtomicNode(baseDir, HEAD_ID),
                    newAtomicNode(baseDir, TAIL_ID),
                    newAtomicNode(baseDir, PROCESSED_ELEMENT_HEAD_ID),
                    nodeBuilder, dir = baseDir, batchSize = batchSize)
        }

        fun newAtomicNode(baseDir: Path, id: String): Atomic<Node<BasicMessage>> {
            val dummy = DiskNode(DUMMY_ID, baseDir, next = null, queue = null)
            return createAtomicNode(dummy, baseDir, id, queue = null)
        }

        val nodeBuilder = fun(item: BasicMessage?, next: Node<BasicMessage>?, timeout: Long, path: Path?): Node<BasicMessage> {
            return DiskNode(item!!.id, path!!, item, next, null, timeout)
        }
    }
}