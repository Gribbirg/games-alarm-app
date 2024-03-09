package com.example.smartalarm.ui.compose.addalarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.smartalarm.R
import com.example.smartalarm.ui.compose.alarms.AlarmsScreen
import com.example.smartalarm.ui.compose.alarms.AlarmsViewModel
import com.example.smartalarm.ui.theme.GamesAlarmTheme

class AddAlarmFragment : Fragment() {
    private lateinit var composeView: ComposeView
    private val viewModel: AddAlarmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).also { composeView = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeView.setContent {
            val state by viewModel.state.collectAsState()
            GamesAlarmTheme {
                AddAlarmScreen(
                    onEvent = viewModel::onEvent,
                    state = state
                )
            }
        }
    }
}