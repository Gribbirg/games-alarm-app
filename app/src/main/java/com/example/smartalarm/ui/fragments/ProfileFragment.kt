package com.example.smartalarm.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.smartalarm.R
import com.example.smartalarm.data.AccountData
import com.example.smartalarm.databinding.FragmentProfileBinding
import com.example.smartalarm.ui.viewmodels.ProfileFragmentViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileFragmentViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ProfileFragmentViewModel::class.java]

        auth = FirebaseAuth.getInstance()

        googleSignInClient = GoogleSignIn.getClient(
            requireActivity(),
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        binding.authButton.setOnClickListener {
            if (binding.authButton.text == "ВОЙТИ")
                singIn()
            else {
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
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {
            setViewAccountData(it)
        }

        return binding.root
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
                Toast.makeText(context, result.resultCode.toString(), Toast.LENGTH_SHORT).show()
        }

    private fun singIn() {
        launcher.launch(googleSignInClient.signInIntent)
    }

    private fun singOut() {
        auth.signOut()
        googleSignInClient.signOut()
        setViewAccountData()
    }

    private fun setViewAccountData(user: AccountData? = null) {
        if (user == null) {
            binding.userNameTextView.text = "Войдите в аккаунт Google"
            binding.userEmailTextView.text = ""
            Glide.with(requireContext())
                .load(R.drawable.baseline_no_accounts_24)
                .into(binding.userPhotoImageView)
            setAuthButtonState(false)
        } else {
            binding.userNameTextView.text = user.name
            binding.userEmailTextView.text = user.email
            Glide.with(requireContext()).load(user.photo).into(binding.userPhotoImageView)
            setAuthButtonState(true)
        }
    }

    private fun setAuthButtonState(enter: Boolean) {
        with(binding.authButton) {
            if (enter) {
                text = "ВЫЙТИ"
                setTextColor(Color.RED)
            } else {
                text = "ВОЙТИ"
                setTextColor(resources.getColor(R.color.green_main))
            }
        }
    }
}