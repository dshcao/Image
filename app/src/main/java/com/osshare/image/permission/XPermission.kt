package com.osshare.image.permission

import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.NonNull
import androidx.collection.ArrayMap
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.lang.Exception
import java.util.*

class XPermission {
    abstract class Callback {
        abstract fun onGrantedAll(requestCode: Int, permissions: Array<String>)
        open fun onGrantedPartials(requestCode: Int, permissions: List<String>) {}
        open fun onDenied(requestCode: Int, permissions: Map<String, Boolean>) {}
        open fun onRevokedByPolicy(requestCode: Int, permissions: List<String>) {}
    }


    companion object {
        private const val TAG_DELEGATE_Fragment = "permission_delegate_fragment"

        @JvmStatic
        fun delegate(@NonNull activity: FragmentActivity): PermissionDelegate {
            val fm = activity.supportFragmentManager

            var delegate: PermissionDelegate? = null
            val fragment = fm.findFragmentByTag(TAG_DELEGATE_Fragment)
            if (fragment != null) {
                if (fragment is PermissionDelegate) {
                    delegate = fragment
                } else {
                    throw Exception("Fragment with tag 'permission_delegate_fragment' is exist,but not PermissionDelegateFragment")
                }
            }

            if (delegate == null) {
                delegate = PermissionDelegateFragment.newInstance()
                fm.beginTransaction().add(delegate, TAG_DELEGATE_Fragment)
                    .commitAllowingStateLoss()
                fm.executePendingTransactions()
            }
            return delegate
        }

    }
}

interface PermissionDelegate {
    fun request(permissions: Array<String>, requestCode: Int, callback: XPermission.Callback?)
}

class PermissionDelegateFragment : Fragment(), PermissionDelegate {
    private val _callbacks = SparseArray<XPermission.Callback>()

    companion object {
        fun newInstance(): PermissionDelegateFragment {
            return PermissionDelegateFragment()
        }
    }

    override fun request(
        permissions: Array<String>,
        requestCode: Int,
        callback: XPermission.Callback?
    ) {
        if (callback != null) _callbacks.put(requestCode, callback)
        requestPermissions(permissions, requestCode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置为 true，表示 configuration change 的时候，fragment 实例不会背重新创建
        retainInstance = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val grants: MutableList<String> = ArrayList()
        val denies: MutableMap<String, Boolean> = ArrayMap()
        val revokedPermissions: MutableList<String> = ArrayList()

        for (i in permissions.indices) {
            val permission = permissions[i]
            if (grantResults[i] == PermissionChecker.PERMISSION_GRANTED) {
                grants.add(permission)
            } else {
                var isShould = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //false 已授权或者被拒绝且选择不在询问,true 被拒绝但没有选择不再询问
                    isShould = shouldShowRequestPermissionRationale(permission)

                    //是否声明了权限
                    if (context!!.packageManager.isPermissionRevokedByPolicy(
                            permission, context!!.packageName
                        )
                    ) {
                        revokedPermissions.add(permission)
                    }

                }
                denies.put(permission, isShould)
            }
        }
        if (denies.isEmpty()) {
            onGrantedAll(requestCode, permissions)
            return
        }
        if (grants.size > 0) {
            onGrantedPartials(requestCode, grants)
        }
        if (revokedPermissions.size > 0) {
            onRevokedByPolicy(requestCode, revokedPermissions)
        }
        onDenied(requestCode, denies)

        _callbacks.remove(requestCode)
    }

    private fun onGrantedAll(requestCode: Int, permissions: Array<String>) {
        _callbacks.get(requestCode).onGrantedAll(requestCode, permissions)
    }

    private fun onGrantedPartials(requestCode: Int, permissions: List<String>) {
        _callbacks.get(requestCode).onGrantedPartials(requestCode, permissions)
    }

    private fun onDenied(requestCode: Int, permissions: Map<String, Boolean>) {
        _callbacks.get(requestCode).onDenied(requestCode, permissions)
    }

    private fun onRevokedByPolicy(requestCode: Int, permissions: List<String>) {
        _callbacks.get(requestCode).onRevokedByPolicy(requestCode, permissions)
    }

    override fun onDetach() {
        super.onDetach()
        _callbacks.clear()
    }
}
