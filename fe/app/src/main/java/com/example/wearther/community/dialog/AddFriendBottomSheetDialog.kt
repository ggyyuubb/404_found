package com.example.wearther.community.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.wearther.databinding.DialogAddFriendBinding
import com.example.wearther.community.data.FriendSuggestion
import com.example.wearther.community.data.SampleData.sampleFriendSuggestions
import com.example.wearther.community.dialog.FriendSuggestionAdapter

class AddFriendBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var friendSuggestionAdapter: FriendSuggestionAdapter
    private val suggestions = mutableListOf<FriendSuggestion>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        loadSuggestions()
    }

    private fun setupRecyclerView() {
        friendSuggestionAdapter = FriendSuggestionAdapter(suggestions) { suggestion ->
            addFriend(suggestion)
        }

        binding.recyclerViewSuggestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendSuggestionAdapter
        }
    }

    private fun setupSearchView() {
        // 검색 기능 구현 (선택사항)
    }

    private fun loadSuggestions() {
        suggestions.clear()
        suggestions.addAll(getSampleSuggestions())
        friendSuggestionAdapter.notifyDataSetChanged()
    }

    private fun getSampleSuggestions(): List<FriendSuggestion> {
        return sampleFriendSuggestions
    }

    private fun addFriend(suggestion: FriendSuggestion) {
        Toast.makeText(requireContext(), "${suggestion.name}님을 팔로우했습니다!", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
