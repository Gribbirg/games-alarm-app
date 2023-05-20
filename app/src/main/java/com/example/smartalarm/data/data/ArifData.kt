package com.example.smartalarm.data.data

data class ArifData(
    val sum: ArrayList<Int>,
    val mult:ArrayList<Int>
) {
    var sumResult = 0
    var multResult = 1
    var sumText = ""
    var multText = ""

    init {
        for (i in sum) {
            sumResult += i
            sumText += "$i + "
        }
        sumText = sumText.substring(0, sumText.length - 2)
        sumText += "= "
        for (i in mult) {
            multResult *= i
            multText += "$i x "
        }
        multText = multText.substring(0, multText.length - 2)
        multText += "= "
    }
}

