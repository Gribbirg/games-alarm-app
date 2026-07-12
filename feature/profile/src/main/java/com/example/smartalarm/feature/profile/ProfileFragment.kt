package com.example.smartalarm.feature.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.smartalarm.feature.profile.R
import com.example.smartalarm.core.data.model.AccountData
import com.example.smartalarm.feature.profile.databinding.FragmentProfileBinding
import com.example.smartalarm.core.ui.AllRecordsAdapter
import com.example.smartalarm.feature.profile.ProfileFragmentViewModel
import com.example.smartalarm.core.data.repositories.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class ProfileFragment : Fragment(), AllRecordsAdapter.OnWorldRecordClickListener {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileFragmentViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ProfileFragmentViewModel::class.java]
        googleSignInClient = AuthRepository.getSignInClient(requireActivity())

        binding.authButton.setOnClickListener {
            singIn()
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {
            setViewAccountData(it)
        }

        viewModel.loadResult.observe(viewLifecycleOwner) {
            if (it != null) {
                Toast.makeText(
                    requireContext(),
                    if (it)
                        "Успешно загруженно"
                    else
                        "Произошла ошибка. Попробуйте снова",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetLoadResult()
                viewModel.getUserRecords()
            }

        }

        viewModel.userRecords.observe(viewLifecycleOwner) {
            if (it.isEmpty() && viewModel.currentUser.value != null) {
                binding.noRecordsTextView.visibility = View.VISIBLE
                binding.noRecordsImageView.visibility = View.VISIBLE
            } else {
                binding.noRecordsTextView.visibility = View.GONE
                binding.noRecordsImageView.visibility = View.GONE
            }
            binding.userRecordsRecycler.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = AllRecordsAdapter(it, this@ProfileFragment)
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserRecords()
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK)
                viewModel.handleAuthResult(
                    GoogleSignIn.getSignedInAccountFromIntent(
                        result.data
                    )
                )
            else
                Toast.makeText(
                    requireContext(),
                    "Произошла ошибка. Попробуйте снова",
                    Toast.LENGTH_LONG
                ).show()
        }

    private fun singIn() {
        launcher.launch(googleSignInClient.signInIntent)
    }

    private fun setViewAccountData(user: AccountData? = null) {
        if (user == null) {
            binding.userNameTextView.text = "Войдите в аккаунт Google"
            binding.userEmailTextView.text = ""
            Glide.with(requireContext())
                .clear(binding.userPhotoImageView)
            binding.userPhotoImageView.setBackgroundResource(R.drawable.baseline_no_accounts_24)
            binding.authButton.visibility = View.VISIBLE
        } else {
            binding.userNameTextView.text = user.name
            binding.userEmailTextView.text = user.email
            binding.userPhotoImageView.setBackgroundResource(0)
            Glide.with(requireContext()).load(user.photo).into(binding.userPhotoImageView)
            binding.authButton.visibility = View.GONE
        }
    }

    override fun onDeleteClickListener(accountData: AccountData) {
        viewModel.deleteRecord(accountData)
    }

    override fun isCurrentUser(accountData: AccountData): Boolean = true
    override fun onProfileClickListener(accountData: AccountData) {}
}