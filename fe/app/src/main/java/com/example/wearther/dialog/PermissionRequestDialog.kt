package com.example.wearther.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.wearther.R
import com.example.wearther.utils.PermissionUtils

class PermissionRequestDialog : DialogFragment() {
    
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    
    companion object {
        private const val ARG_PERMISSION_GROUP = "permission_group"
        private const val ARG_PERMISSION_DESCRIPTION = "permission_description"
        
        fun newInstance(
            permissionGroup: String,
            permissionDescription: String,
            onResult: (Boolean) -> Unit
        ): PermissionRequestDialog {
            return PermissionRequestDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PERMISSION_GROUP, permissionGroup)
                    putString(ARG_PERMISSION_DESCRIPTION, permissionDescription)
                }
                onPermissionResult = onResult
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.PermissionDialogStyle)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_permission_request, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val permissionGroup = arguments?.getString(ARG_PERMISSION_GROUP) ?: ""
        val permissionDescription = arguments?.getString(ARG_PERMISSION_DESCRIPTION) ?: ""
        
        // 다이얼로그 제목 설정
        view.findViewById<TextView>(R.id.tvDialogTitle).text = "$permissionGroup 접근 권한"
        
        // 권한 설명 설정
        view.findViewById<TextView>(R.id.tvPermissionDescription).text = permissionDescription
        
        // 권한 그룹에 따른 아이콘 설정
        val iconResId = when (permissionGroup) {
            "위치 정보" -> R.drawable.ic_permission_location
            "카메라" -> R.drawable.ic_permission_camera
            "앨범" -> R.drawable.ic_permission_gallery
            else -> R.drawable.ic_permission_location
        }
        view.findViewById<android.widget.ImageView>(R.id.ivPermissionIcon).setImageResource(iconResId)
        
        // 허용 버튼
        view.findViewById<Button>(R.id.btnAllow).setOnClickListener {
            onPermissionResult?.invoke(true)
            dismiss()
        }
        
        // 거부 버튼
        view.findViewById<Button>(R.id.btnDeny).setOnClickListener {
            onPermissionResult?.invoke(false)
            dismiss()
        }
        
        // 다이얼로그 외부 클릭 시 닫기 방지
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
    }
} 