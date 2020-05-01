package jasper.wagner.cryptotracking.common

import kotlin.math.pow

object MathOperation {

    fun round(value: Double): Double{
        var newValue = value
        val factor = 10.0.pow(2)
        newValue *= factor
        val tmp = Math.round(newValue)
        return tmp / factor
    }
}
