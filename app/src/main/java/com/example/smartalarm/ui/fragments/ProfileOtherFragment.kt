package com.example.smartalarm.ui.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.smartalarm.ui.viewmodels.ProfileOtherViewModel
import com.example.smartalarm.R
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.databinding.FragmentProfileOtherBinding
import com.example.smartalarm.ui.adapters.AllRecordsAdapter

class ProfileOtherFragment : Fragment(), AllRecordsAdapter.OnWorldRecordClickListener {
    private lateinit var binding: FragmentProfileOtherBinding
    private lateinit var viewModel: ProfileOtherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Navigation.findNavController(binding.root).navigate(
                        R.id.action_profileOtherFragment_to_recordsFragment,
                        null,
                        NavOptions.Builder().setPopUpTo(R.id.alarmsFragment, true).build()
                    )
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileOtherBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ProfileOtherViewModel::class.java]

        viewModel.user.observe(viewLifecycleOwner) {
            viewModel.getRecords()
            binding.userNameOtherTextView.text = it.name
            binding.userEmailOtherTextView.text = it.email
            binding.userPhotoOtherImageView.setBackgroundResource(0)
            Glide.with(requireContext()).load(it.photo).into(binding.userPhotoOtherImageView)
        }

        viewModel.userRecords.observe(viewLifecycleOwner) {
            binding.userRecordsOtherRecycler.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = AllRecordsAdapter(it, this@ProfileOtherFragment)
            }

        }

        viewModel.getUser(requireArguments().getString("user uid")!!)

        return binding.root
    }

    override fun onDeleteClickListener(accountData: AccountData) {}

    override fun isCurrentUser(accountData: AccountData): Boolean = false

    override fun onProfileClickListener(accountData: AccountData) {}
}