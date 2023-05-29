package com.example.smartalarm.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.smartalarm.R
import com.example.smartalarm.databinding.FragmentSettingsBinding
import com.example.smartalarm.ui.viewmodels.SettingsFragmentViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsFragmentViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SettingsFragmentViewModel::class.java]
        googleSignInClient = GoogleSignIn.getClient(
            requireActivity(),
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        viewModel.loadResult.observe(viewLifecycleOwner) {
            if (it != null) {
                Toast.makeText(
                    requireContext(),
                    if (it)
                        "Успешно"
                    else
                        "Произошла ошибка. Попробуйте снова",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetLoadResult()
            }
        }

        binding.loadAlarmsButton.setOnClickListener {
            if (!viewModel.loadAlarmsOfCurrentUser())
                Toast.makeText(requireContext(), "Войдите в аккаунт!", Toast.LENGTH_SHORT).show()
        }

        binding.loadAlarmsFromButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Подтверждение")
                .setIcon(R.drawable.baseline_warning_24)
                .setMessage("Вы уверены, что хотите загрузить будильники?\nВсе текущие будильники будут удалены.")
                .setPositiveButton("Да") { dialog, _ ->
                    if (!viewModel.loadAlarmsFromInternet())
                        Toast.makeText(requireContext(), "Войдите в аккаунт!", Toast.LENGTH_SHORT)
                            .show()
                    dialog.dismiss()
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.deleteAlarmsFromCloudButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Удаление будильников")
                .setIcon(R.drawable.baseline_warning_24)
                .setMessage("Вы уверены, что хотите удалить все будильники из облака?")
                .setPositiveButton("Да") { dialog, _ ->
                    if (!viewModel.deleteAlarmsFromCloud())
                        Toast.makeText(requireContext(), "Войдите в аккаунт!", Toast.LENGTH_SHORT)
                            .show()
                    dialog.dismiss()
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.deleteAllRecordsFromAccountButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Удаление результатов")
                .setIcon(R.drawable.baseline_warning_24)
                .setMessage("Вы уверены, что хотите удалить все результы из облака?")
                .setPositiveButton("Да") { dialog, _ ->
                    if (!viewModel.deleteRecordsFromCloud())
                        Toast.makeText(requireContext(), "Войдите в аккаунт!", Toast.LENGTH_SHORT)
                            .show()
                    dialog.dismiss()
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Удаление аккаунта")
                .setIcon(R.drawable.baseline_warning_24)
                .setMessage("Вы уверены, что хотите удалить аккаунт?")
                .setPositiveButton("Да") { dialog, _ ->
                    if (!viewModel.deleteAccountCloud()) {
                        Toast.makeText(requireContext(), "Войдите в аккаунт!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        googleSignInClient.signOut()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.exitButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Выход из аккаунта")
                .setIcon(R.drawable.baseline_warning_24)
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Да") { dialog, _ ->
                    singOut()
                    dialog.dismiss()
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.deleteAllRecordsButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Удаление результов")
                .setIcon(R.drawable.baseline_warning_24)
                .setMessage("Вы уверены, что хотите удалить все результаты с устройства?")
                .setPositiveButton("Да") { dialog, _ ->
                    viewModel.deleteAllRecordsFromDb()
                    dialog.dismiss()
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.deleteAllAlarmsButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Удаление будильников")
                .setIcon(R.drawable.baseline_warning_24)
                .setMessage("Вы уверены, что хотите удалить все будильники с устройства?")
                .setPositiveButton("Да") { dialog, _ ->
                    viewModel.deleteAllAlarmsFromDb()
                    dialog.dismiss()
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        return binding.root
    }

    private fun singOut() {
        if (!viewModel.exitFromAccount())
            Toast.makeText(
                requireContext(),
                "Сначала войдите в аккаунт!",
                Toast.LENGTH_SHORT
            )
                .show()
        else
            googleSignInClient.signOut()
    }
}