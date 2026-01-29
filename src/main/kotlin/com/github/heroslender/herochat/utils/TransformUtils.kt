package com.github.heroslender.herochat.utils

import com.hypixel.hytale.math.vector.Vector3d

fun Vector3d.distanceSquared(other: Vector3d): Double {
    return square(x - other.x) + square(y - other.y) + square(z - other.z)
}