package com.example.wearther.community.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.wearther.R
import com.example.wearther.community.data.UserProfile
import com.google.android.material.imageview.ShapeableImageView

class UserProfileDialog : DialogFragment() {
    
    private var onAddFriendClick: ((String) -> Unit)? = null
    
    companion object {
        private const val ARG_USER_PROFILE = "user_profile"
        
        fun newInstance(
            userProfile: UserProfile,
            onAddFriend: (String) -> Unit
        ): UserProfileDialog {
            return UserProfileDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_USER_PROFILE, userProfile)
                }
                onAddFriendClick = onAddFriend
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.UserProfileDialogStyle)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_user_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val userProfile = arguments?.getParcelable<UserProfile>(ARG_USER_PROFILE)
        userProfile?.let { profile ->
            setupUserProfile(view, profile)
        }
        
        // 다이얼로그 외부 클릭 시 닫기
        dialog?.setCanceledOnTouchOutside(true)
    }
    
    private fun setupUserProfile(view: View, profile: UserProfile) {
        // 프로필 이미지
        val ivProfile = view.findViewById<ShapeableImageView>(R.id.ivUserProfile)
        Glide.with(this)
            .load(profile.userProfileImage)
            .placeholder(R.drawable.gradient_profile_bg)
            .circleCrop()
            .into(ivProfile)
        
        // 사용자명
        view.findViewById<TextView>(R.id.tvUserName).text = profile.userName
        
        // 이메일
        view.findViewById<TextView>(R.id.tvUserEmail).text = profile.userEmail
        
        // 통계 정보
        view.findViewById<TextView>(R.id.tvFollowerCount).text = "${profile.followerCount}"
        view.findViewById<TextView>(R.id.tvFollowingCount).text = "${profile.followingCount}"
        view.findViewById<TextView>(R.id.tvPostCount).text = "${profile.postCount}"
        
        // 친구 추가 버튼
        val btnAddFriend = view.findViewById<Button>(R.id.btnAddFriend)
        if (profile.isFriend) {
            btnAddFriend.text = "친구"
            btnAddFriend.isEnabled = false
            btnAddFriend.setBackgroundResource(R.drawable.button_disabled_background)
        } else {
            btnAddFriend.text = "친구 추가"
            btnAddFriend.setOnClickListener {
                onAddFriendClick?.invoke(profile.userId)
                dismiss()
            }
        }
    }
} 