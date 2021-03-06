/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.app

import com.almasb.fxgl.animation.Animation
import com.almasb.fxgl.input.UserAction
import com.almasb.fxgl.texture.Texture
import com.almasb.fxgl.util.EmptyRunnable
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.SnapshotParameters
import javafx.scene.effect.DropShadow
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeType
import javafx.util.Duration

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
internal object PauseMenuSubState : SubState() {

    private val masker = Rectangle(FXGL.getAppWidth().toDouble(), FXGL.getAppHeight().toDouble(), Color.color(0.0, 0.0, 0.0, 0.25))
    private val content: Pane

    private var canSwitchGameMenu = true

    private val animation: Animation<*>

    init {
        input.addAction(object : UserAction("Resume") {
            override fun onActionBegin() {
                requestHide()
            }

            override fun onActionEnd() {
                unlockSwitch()
            }
        }, FXGL.getSettings().menuKey)

        content = createContentPane()
        content.children.add(createContent())

        content.translateX = FXGL.getAppWidth() / 2.0 - 125
        content.translateY = FXGL.getAppHeight() / 2.0 - 200

        children.addAll(masker, content)

        animation = FXGL.getUIFactory().translate(content,
                Point2D(FXGL.getAppWidth() / 2.0 - 125, -400.0),
                Point2D(FXGL.getAppWidth() / 2.0 - 125, FXGL.getAppHeight() / 2.0 - 200),
                Duration.seconds(0.33))
    }

    override fun onEnter(prevState: State) {
        if (prevState !is PlayState) {
            throw IllegalArgumentException("Entered PauseState from illegal state $prevState")
        }

        animation.onFinished = EmptyRunnable
        animation.start(this)
    }

    internal fun requestShow() {
        if (canSwitchGameMenu) {
            canSwitchGameMenu = false
            FXGL.getApp().stateMachine.pushState(this)
        }
    }

    private fun requestHide() {
        if (animation.isAnimating)
            return

        if (canSwitchGameMenu) {
            canSwitchGameMenu = false

            animation.onFinished = Runnable {
                FXGL.getApp().stateMachine.popState()
            }
            animation.startReverse(this)
        }
    }

    internal fun unlockSwitch() {
        canSwitchGameMenu = true
    }

    private fun createContentPane(): StackPane {
        val dx = 20.0
        val dy = 15.0
        val width = 250.0
        val height = 400.0

        val outer = Polygon(
                0.0, dx,
                dx, 0.0,
                width * 2 / 3, 0.0,
                width * 2 / 3, 2 * dy,
                width * 2 / 3 + 3 * dx, 2 * dy,
                width * 2 / 3 + 3 * dx, 0.0,
                width - dx, 0.0,
                width, dx,
                width, height - dy,
                width - dy, height,
                dy, height,
                0.0, height - dy,
                0.0, height / 3 + 4 * dy,
                dx, height / 3 + 5 * dy,
                dx, height / 3 + 3 * dy,
                0.0, height / 3 + 2 * dy,
                0.0, height / 3,
                dx, height / 3 + dy,
                dx, height / 3 - dy,
                0.0, height / 3 - 2 * dy
        )

        outer.fill = Color.BLACK
        outer.stroke = Color.AQUA
        outer.strokeWidth = 6.0
        outer.strokeType = StrokeType.CENTERED
        outer.effect = DropShadow(15.0, Color.BLACK)

        val params = SnapshotParameters()
        params.fill = Color.TRANSPARENT

        // draw into image to speed up rendering
        val image = outer.snapshot(params, null)

        return StackPane(Texture(image))
    }

    private fun createContent(): Parent {
        val btnResume = FXGL.getUIFactory().newButton("RESUME")
        btnResume.setOnAction {
            requestHide()
            unlockSwitch()
        }

        val btnExit = FXGL.getUIFactory().newButton("EXIT")
        btnExit.setOnAction {
            FXGL.getApp().exit()
        }

        val vbox = VBox(15.0, btnResume, btnExit)
        vbox.alignment = Pos.CENTER
        vbox.setPrefSize(250.0, 400.0)

        return vbox
    }
}