package io.github.lucaargolo.extragenerators.utils

import io.github.lucaargolo.extragenerators.common.block.BlockCompendium
import net.minecraft.util.Identifier
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.math.*

object ActiveGenerators {

    private val UUIDMap = linkedMapOf<UUID, LinkedHashMap<Identifier, Int>>()
    private var lastUUIDMap = linkedMapOf<UUID, LinkedHashMap<Identifier, Int>>()

    private val infiniteMap = linkedMapOf<UUID, Int>()

    fun get(uuid: UUID?) = lastUUIDMap.getOrDefault(uuid, linkedMapOf())

    fun add(uuid: UUID, identifier: Identifier) {
        val value = UUIDMap.getOrPut(uuid) { linkedMapOf() }.getOrPut(identifier) { 0 }
        UUIDMap.getValue(uuid)[identifier] = value+1
    }

    fun tick() {
        infiniteMap.clear()
        UUIDMap.forEach { (uuid, uuidSpecificMap) ->
            var minValue = Int.MAX_VALUE
            BlockCompendium.generatorIdentifierArray().forEach {
                minValue = min(minValue, uuidSpecificMap.getOrDefault(it, 0))
            }
            infiniteMap[uuid] = minValue*2
        }
        lastUUIDMap = LinkedHashMap(UUIDMap)
        UUIDMap.clear()
    }

    //Test if passed map is equal to the last stored map
    fun test(uuid: UUID?, test: LinkedHashMap<Identifier, Int>): Boolean {
        val activeGenerators = lastUUIDMap.getOrDefault(uuid, linkedMapOf())
        return test.entries.containsAll(activeGenerators.entries)
    }

    //Test if uuid can run infinite generators
    fun test(uuid: UUID): Boolean {
        val inf = infiniteMap.getOrDefault(uuid, 0)
        return if(inf >= 1) {
            infiniteMap[uuid] = inf-1
            true
        }else {
            false
        }
    }

}