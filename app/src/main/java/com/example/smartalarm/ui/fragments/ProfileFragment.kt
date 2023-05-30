package com.example.smartalarm.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.smartalarm.R
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.databinding.FragmentProfileBinding
import com.example.smartalarm.ui.adapters.AllRecordsAdapter
import com.example.smartalarm.ui.viewmodels.ProfileFragmentViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.color.MaterialColors
import com.google.firebase.auth.FirebaseAuth

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
        googleSignInClient = GoogleSignIn.getClient(
            requireActivity(),
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

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