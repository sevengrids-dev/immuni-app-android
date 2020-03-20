package com.bendingspoons.ascolto.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.models.survey.CompositeAnswer
import com.bendingspoons.ascolto.models.survey.PickerWidget
import com.bendingspoons.ascolto.models.survey.Survey
import com.bendingspoons.ascolto.ui.log.fragment.FormContentFragment
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.visible
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.form_picker_field.*
import kotlinx.android.synthetic.main.form_text_field.next


class PickerFieldFragment: FormContentFragment(R.layout.form_picker_field) {
    override val nextButton: Button
        get() = next
    override val prevButton: ImageView
        get() = back
    override val questionText: TextView
        get() = question
    override val descriptionText: TextView
        get() = description

    val items = mutableListOf<NumberPicker>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.survey.observe(viewLifecycleOwner, Observer {
            buildWidget(it)
        })

        nextButton.isEnabled = true
    }

    private fun buildWidget(it: Survey) {
        val question = it.questions.first { it.id == questionId }

        questionText.text = question.title
        descriptionText.text = question.description

        if(question.description.isEmpty()) descriptionText.gone()
        else descriptionText.visible()

        val widget = question.widget as PickerWidget
        widget.components.forEachIndexed { index, picker ->
            items.apply {
                add(NumberPicker(context).apply {
                    tag = index
                    // IMPORTANT! setMinValue to 1 and call setDisplayedValues after setMinValue and setMaxValue
                    val data = picker.toTypedArray()
                    minValue = 1
                    maxValue = data.size
                    displayedValues = data
                    value = 1
                    setOnValueChangedListener { picker, oldVal, newVal ->
                        validate()
                    }
                    dividerColor = resources.getColor(R.color.picker_divider_color)
                    textColor = Color.parseColor("#495D74")
                    selectedTextColor = Color.parseColor("#495D74")
                })
            }
        }

        pickerGroup.apply {
            items.forEach { addView(it) }
        }

        formModel()?.let {
            onFormModelUpdate(it)
        }
    }

    override fun onFormModelUpdate(model: FormModel) {
        model.answers[questionId]?.let {
            (it as CompositeAnswer).componentIndexes.forEachIndexed { pickerIndex, valueIndex ->
                if(items.size > pickerIndex) {
                    items[pickerIndex].value = valueIndex
                }
            }
        }
    }

    override fun validate(): Boolean {
        val valid = true
        nextButton.isEnabled = valid
        if(valid) saveData()
        return valid
    }

    private fun saveData() {
        val indexes = items.map { it.value }
        viewModel.saveAnswer(questionId, CompositeAnswer(indexes))
    }
}