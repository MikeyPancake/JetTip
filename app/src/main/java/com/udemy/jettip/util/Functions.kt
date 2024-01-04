package com.udemy.jettip.util

fun calculateTotalTip(totalBill: Double, tipPercentage: Int): Double {

    return if (totalBill > 1 && totalBill.toString().isNotEmpty())
        (totalBill * tipPercentage) / 100 else 0.0
}

/**
 * Function for calculating total bill using the bill total, number of splits, and the tip percentage.
 * The function then returns a Double which is used in the top header
 */
fun calculateTotalPerPersonBill(
    totalBill: Double,
    splitBy : Int,
    tipPercentage: Int): Double{

    // call calculate tip function and gets the total tip then adds that to the total bill
    val bill = calculateTotalTip(totalBill = totalBill,
        tipPercentage = tipPercentage) + totalBill

    // returns the double of the total bill divided by the split total
    return (bill / splitBy)
}