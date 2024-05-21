package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.libs.DaytimeModel

class RepeatingFormSheetVM(
    private val repeating: RepeatingDb?
) : __VM<RepeatingFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val textFeatures: TextFeatures,
        val period: RepeatingDb.Period?,
        val daytimeModel: DaytimeModel?,
        val isImportant: Boolean,
    ) {

        val periodTitle = "Period"
        val periodNote: String = period?.title ?: "Not Selected"
        val periodFsDoneText = "Done"

        val daytimeHeader = "Time of the Day"
        val daytimeNote: String = daytimeModel?.text ?: "None"

        val defDaytimeModel: DaytimeModel = daytimeModel ?: DaytimeModel(hour = 12, minute = 0)

        val isImportantHeader = "Is Important"

        val inputTextValue = textFeatures.textNoFeatures
    }

    override val state: MutableStateFlow<State> = MutableStateFlow(
        State(
            headerTitle = if (repeating != null) "Edit Repeating" else "New Repeating",
            headerDoneText = if (repeating != null) "Save" else "Create",
            textFeatures = (repeating?.text ?: "").textFeatures(),
            period = repeating?.getPeriod(),
            daytimeModel = repeating?.daytime?.let { DaytimeModel.byDaytime(it) },
            isImportant = repeating?.is_important?.toBoolean10() ?: false,
        )
    )

    fun setTextValue(text: String) {
        state.update { it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text)) }
    }

    fun upTextFeatures(textFeatures: TextFeatures) {
        state.update { it.copy(textFeatures = textFeatures) }
    }

    fun setPeriod(period: RepeatingDb.Period?) {
        state.update { it.copy(period = period) }
    }

    fun upDaytime(daytimeModel: DaytimeModel?) {
        state.update { it.copy(daytimeModel = daytimeModel) }
    }

    fun toggleIsImportant() {
        state.update { it.copy(isImportant = !it.isImportant) }
    }

    fun save(
        onSuccess: () -> Unit,
    ) = scopeVM().launchEx {
        try {
            val textFeatures = state.value.textFeatures
            if (textFeatures.textNoFeatures.isBlank())
                throw UIException("No text")
            if (textFeatures.activity == null)
                throw UIException("Activity not selected")
            if (textFeatures.timer == null)
                throw UIException("Timer not selected")

            val nameWithFeatures = textFeatures.textWithFeatures()
            val isImportant = state.value.isImportant

            val daytime: Int? = state.value.daytimeModel?.seconds

            val period = state.value.period ?: throw UIException("Period not selected")

            if (repeating != null) {
                repeating.upWithValidation(
                    text = nameWithFeatures,
                    period = period,
                    daytime = daytime,
                    isImportant = isImportant,
                )
                TaskDb.getAsc().forEach { task ->
                    val tf = task.text.textFeatures()
                    if (tf.fromRepeating?.id == repeating.id) {
                        val newTf = tf.copy(isImportant = isImportant)
                        task.upTextWithValidation(newTf.textWithFeatures())
                    }
                }
            } else {
                val lastDay: Int = if (period is RepeatingDb.Period.EveryNDays && period.nDays == 1)
                    UnixTime().localDay - 1
                else
                    UnixTime().localDay

                RepeatingDb.addWithValidation(
                    text = nameWithFeatures,
                    period = period,
                    lastDay = lastDay,
                    daytime = daytime,
                    isImportant = isImportant,
                )

                RepeatingDb.syncTodaySafe(RepeatingDb.todayWithOffset())
            }
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
