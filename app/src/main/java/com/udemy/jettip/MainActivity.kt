package com.udemy.jettip

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udemy.jettip.components.InputField
import com.udemy.jettip.ui.theme.JetTipTheme
import com.udemy.jettip.util.calculateTotalPerPersonBill
import com.udemy.jettip.util.calculateTotalTip
import com.udemy.jettip.widgets.RoundIconButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserInterface {
                MainContent()
            }

        }
    }
}

//Container function that holds the entire UI
@Composable
fun UserInterface(content: @Composable () -> Unit){

    JetTipTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
            }
        }
    }
}

@Preview
@Composable
fun MainContent(){

    // holds state of the text input box as a string since outputs are always strings
    val totalBillState = remember{
        mutableStateOf("")
    }

    // State allows us to check text field is valid
    val validState = remember(totalBillState.value) { // pass the value we want to check
        totalBillState.value.trim().isNotEmpty() // if state has data, trim it
    }

    // State that holds the sliders value
    val sliderPositionState = remember{
        mutableStateOf(0f)
    }

    // State controller for the split by buttons
    val splitByState = remember{
        mutableStateOf(1)
    }

    // State holder for tip amount that is used in the slider
    val tipAmountState = remember{
        mutableStateOf(0.0)
    }

    // State controller for the total amount per pserson
    val totalPerPersonState = remember {
        mutableStateOf(0.0)
    }

    Column(
        modifier = Modifier
            .padding(10.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BillForm(
            totalBillState = totalBillState,
            validState = validState,
            sliderPositionState = sliderPositionState,
            splitByState = splitByState,
            tipAmountState = tipAmountState,
            totalPerPersonState = totalPerPersonState) {}

    }


}

@Composable
fun BillForm(
    modifier: Modifier = Modifier,
    totalBillState :MutableState<String>,
    validState : Boolean,
    sliderPositionState: MutableState<Float>,
    splitByState : MutableState<Int>,
    tipAmountState : MutableState<Double>,
    totalPerPersonState : MutableState<Double>,
    onValueChange: (String) -> Unit = {}
){

    // Takes the slider position value and multiplies it to get the percentage
    val tipPercentage = (sliderPositionState.value * 100).toInt()

    // used to manipulate the keyboard focus
    val focusManager = LocalFocusManager.current

    // Adds Top Header to form
    TopHeader(totalAmount = totalPerPersonState.value)

    Surface (modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth(),
        shape = RoundedCornerShape(
            corner = CornerSize(8.dp)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Column(modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Composable that holds the total bill amount input box
            TotalBillInputText(
                totalBillState,
                validState,
                onValueChange,
                focusManager
            )

            if(validState){
                // Main row that holds the Split view items
                SplitByRow(
                    splitByState,
                    totalPerPersonState,
                    totalBillState,
                    tipPercentage
                )

                // Tip Row
                TipRow(tipAmountState)

                // Composable for the Slider tip Column
                SliderTipPercentageColumn(
                    tipPercentage,
                    sliderPositionState,
                    tipAmountState,
                    totalBillState,
                    totalPerPersonState,
                    splitByState
                )

            }
            else{
                // If state has not changed, hid the bill form
                Box(){}
            }
        }
    }
}

@Composable
private fun TotalBillInputText(
    totalBillState: MutableState<String>,
    validState: Boolean,
    onValueChange: (String) -> Unit,
    focusManager: FocusManager
) {
    InputField(
        valueState = totalBillState,
        labelId = "Enter Bill Total",
        enabled = true,
        isSingleLine = true,
        onAction = KeyboardActions {
            if (!validState) return@KeyboardActions
            // Assigns the callback (onValueCHanged) the amount entered by the user
            onValueChange(totalBillState.value.trim())
            // CLoses the keyboard once enter is clicked
            focusManager.clearFocus()
        }
    )
}

@Composable
private fun SliderTipPercentageColumn(
    tipPercentage: Int,
    sliderPositionState: MutableState<Float>,
    tipAmountState: MutableState<Double>,
    totalBillState: MutableState<String>,
    totalPerPersonState: MutableState<Double>,
    splitByState: MutableState<Int>
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Percentage Text Row
        Text(
            text = "$tipPercentage%",
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        // Slider for adjusting tip amount
        Slider(
            value = sliderPositionState.value,
            onValueChange = { newVal ->
                sliderPositionState.value = newVal
                Log.d("Tip Slider", "Bill Form: $newVal")
                tipAmountState.value =
                    calculateTotalTip(
                        totalBill = totalBillState.value.toDouble(),
                        tipPercentage = tipPercentage
                    )

                totalPerPersonState.value =
                    calculateTotalPerPersonBill(
                        totalBill = totalBillState.value.toDouble(),
                        splitBy = splitByState.value,
                        tipPercentage = tipPercentage
                    )
            },
            modifier = Modifier.padding(10.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                // active color properties
                activeTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                //activeTickColor = MaterialTheme.colorScheme.onPrimary,
                // Inactive color properties
                inactiveTrackColor = MaterialTheme.colorScheme.onTertiary,
                //inactiveTickColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
            onValueChangeFinished = {}
        )
    }
}

@Composable
private fun TipRow(tipAmountState: MutableState<Double>) {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Tip",
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        // Spacer between "tip" and Amount
        Spacer(modifier = Modifier.width(170.dp))

        Text(
            text = "$ ${tipAmountState.value}",
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SplitByRow(
    splitByState: MutableState<Int>,
    totalPerPersonState: MutableState<Double>,
    totalBillState: MutableState<String>,
    tipPercentage: Int
) {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Split",
            modifier = Modifier
                .align(alignment = Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.width(120.dp))

        // Row that holds the increment/decrement buttons
        Row(
            modifier = Modifier
                .padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.End
        ) {
            // Decrease split number button
            RoundIconButton(
                imageVector = Icons.Default.Remove,
                onClick = { /*TODO*/
                    Log.d("Bill Form", " Decreased pressed")
                    // takes the value of split by and allows it to be decreased no lower than 1
                    splitByState.value =
                        if (splitByState.value > 1) {
                            splitByState.value - 1
                        } else {
                            1
                        }
                    // Updates Per Per person amount
                    totalPerPersonState.value =
                        calculateTotalPerPersonBill(
                            totalBill = totalBillState.value.toDouble(),
                            splitBy = splitByState.value,
                            tipPercentage = tipPercentage
                        )
                }
            )
            // Displayed split number Text
            Text(
                text = "${splitByState.value}",
                modifier = Modifier
                    .align(alignment = Alignment.CenterVertically)
                    .padding(5.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            // Increase split number button
            RoundIconButton(
                imageVector = Icons.Default.Add,
                onClick = { /*TODO*/
                    Log.d("Bill Form", " Increased pressed")
                    splitByState.value =
                            // Sets max range to 20
                        if (splitByState.value < 20) {
                            splitByState.value + 1
                        } else {
                            20
                        }
                    // Updates Per Per person amount
                    totalPerPersonState.value =
                        calculateTotalPerPersonBill(
                            totalBill = totalBillState.value.toDouble(),
                            splitBy = splitByState.value,
                            tipPercentage = tipPercentage
                        )
                }
            )
        }
    }
}

// Top Header that displays Total Per Person
@Composable
fun TopHeader(totalAmount : Double = 134.00){
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .padding(10.dp),
        //.clip(shape = CircleShape.copy(all = CornerSize(12.dp))),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        // Defines the color of the shape
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline
        ),
    ) {
        Column(modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Format number to display 2 decimal places
            val amountPerPerson = "%.2f".format(totalAmount)
            Text(text = "Total Per Person",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)

            Text(text = "$$amountPerPerson",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JetTipTheme {
        UserInterface {
            //TopHeader()
            MainContent()
        }
    }
}