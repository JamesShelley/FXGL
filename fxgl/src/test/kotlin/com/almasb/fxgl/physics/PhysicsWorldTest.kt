/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.physics

import com.almasb.fxgl.app.FXGL
import com.almasb.fxgl.ecs.Entity
import com.almasb.fxgl.ecs.GameWorld
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class PhysicsWorldTest {

    companion object {
        @BeforeClass
        @JvmStatic fun before() {
            FXGL.configure(com.almasb.fxgl.app.MockApplicationModule.get())
        }
    }

    private enum class EntityType {
        TYPE1, TYPE2
    }

    private val physicsWorld = FXGL.getInstance(com.almasb.fxgl.physics.PhysicsWorld::class.java)

    @Test
    fun `Collision notification`() {
        val entity1 = com.almasb.fxgl.entity.Entities.builder()
                .type(EntityType.TYPE1)
                .at(100.0, 100.0)
                .bbox(com.almasb.fxgl.physics.HitBox("Test1", com.almasb.fxgl.physics.BoundingShape.box(40.0, 40.0)))
                .with(com.almasb.fxgl.entity.component.CollidableComponent(true))
                .build()

        val entity2 = com.almasb.fxgl.entity.Entities.builder()
                .type(EntityType.TYPE2)
                .at(150.0, 100.0)
                .bbox(com.almasb.fxgl.physics.HitBox("Test2", com.almasb.fxgl.physics.BoundingShape.box(40.0, 40.0)))
                .with(com.almasb.fxgl.entity.component.CollidableComponent(true))
                .build()

        var hitboxCount = 0
        var collisionBeginCount = 0
        var collisionCount = 0
        var collisionEndCount = 0

        val handler = object : com.almasb.fxgl.physics.CollisionHandler(EntityType.TYPE1, EntityType.TYPE2) {

            override fun onHitBoxTrigger(a: Entity, b: Entity, boxA: com.almasb.fxgl.physics.HitBox, boxB: com.almasb.fxgl.physics.HitBox) {
                assertTrue(a === entity1)
                assertTrue(b === entity2)

                assertThat(boxA.name, `is`("Test1"))
                assertThat(boxB.name, `is`("Test2"))

                hitboxCount++
            }

            override fun onCollisionBegin(a: Entity, b: Entity) {
                assertTrue(a === entity1)
                assertTrue(b === entity2)
                collisionBeginCount++
            }

            override fun onCollision(a: Entity, b: Entity) {
                assertTrue(a === entity1)
                assertTrue(b === entity2)
                collisionCount++
            }

            override fun onCollisionEnd(a: Entity, b: Entity) {
                assertTrue(a === entity1)
                assertTrue(b === entity2)
                collisionEndCount++
            }
        }

        physicsWorld.addCollisionHandler(handler)

        // create game world and add listener
        val gameWorld = FXGL.getInstance(GameWorld::class.java)
        gameWorld.addWorldListener(physicsWorld)

        gameWorld.addEntity(entity1)
        gameWorld.addEntity(entity2)
        gameWorld.onUpdate(0.016)

        // no collision happened, entities are apart
        assertThat(hitboxCount, `is`(0))
        assertThat(collisionBeginCount, `is`(0))
        assertThat(collisionCount, `is`(0))
        assertThat(collisionEndCount, `is`(0))

        // move 2nd entity closer to first, colliding with it
        entity2.translateX(-30.0)

        gameWorld.onUpdate(0.016)

        // hit box and collision begin triggered, entities are now colliding
        assertThat(hitboxCount, `is`(1))
        assertThat(collisionBeginCount, `is`(1))
        assertThat(collisionCount, `is`(1))
        assertThat(collisionEndCount, `is`(0))

        gameWorld.onUpdate(0.016)

        // collision continues
        assertThat(hitboxCount, `is`(1))
        assertThat(collisionBeginCount, `is`(1))
        assertThat(collisionCount, `is`(2))
        assertThat(collisionEndCount, `is`(0))

        gameWorld.onUpdate(0.016)

        // collision continues
        assertThat(hitboxCount, `is`(1))
        assertThat(collisionBeginCount, `is`(1))
        assertThat(collisionCount, `is`(3))
        assertThat(collisionEndCount, `is`(0))

        // move 2nd entity away from 1st
        entity2.translateX(30.0)

        gameWorld.onUpdate(0.016)

        // collision end
        assertThat(hitboxCount, `is`(1))
        assertThat(collisionBeginCount, `is`(1))
        assertThat(collisionCount, `is`(3))
        assertThat(collisionEndCount, `is`(1))

        physicsWorld.removeCollisionHandler(handler)

        // move 2nd entity closer to 1st, colliding with it
        entity2.translateX(-30.0)

        gameWorld.onUpdate(0.016)

        // no change in collision
        assertThat(hitboxCount, `is`(1))
        assertThat(collisionBeginCount, `is`(1))
        assertThat(collisionCount, `is`(3))
        assertThat(collisionEndCount, `is`(1))
    }
}