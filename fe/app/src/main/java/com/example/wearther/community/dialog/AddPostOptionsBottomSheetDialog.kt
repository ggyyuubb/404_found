package com.example.wearther.community.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.wearther.databinding.DialogAddPostOptionsBinding
import com.example.wearther.community.data.PostSourceOption
import com.example.wearther.community.screen.PostOptionsAdapter

class AddPostOptionsBottomSheetDialog(
    private val onOptionSelected: (PostSourceOption) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogAddPostOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var optionsAdapter: PostOptionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddPostOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        optionsAdapter = PostOptionsAdapter(PostSourceOption.values().toList()) { option ->
            onOptionSelected(option)
            dismiss()
        }

        binding.recyclerViewOptions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = optionsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
