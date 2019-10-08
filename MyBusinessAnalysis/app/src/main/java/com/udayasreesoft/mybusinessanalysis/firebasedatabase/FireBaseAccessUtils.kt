package com.udayasreesoft.mybusinessanalysis.firebasedatabase

import android.content.Context
import com.google.firebase.database.*
import com.udayasreesoft.mybusinessanalysis.utils.PreferenceSharedUtils

class FireBaseAccessUtils(val context: Context) {

    private var fireBaseAccessUtils : FireBaseAccessUtils? = null

    @Synchronized
    fun getInstance() : FireBaseAccessUtils{
        if (fireBaseAccessUtils == null) {
            fireBaseAccessUtils = FireBaseAccessUtils(context)
        }
        return fireBaseAccessUtils as FireBaseAccessUtils
    }

    fun writeUserToDataBase(userSignInModel: UserSignInModel) : Boolean {
        var isSuccess = false
        with (userSignInModel) {
            FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userOutlet)
                .child(userId)
                .setValue(userSignInModel, object : DatabaseReference.CompletionListener{
                    override fun onComplete(error: DatabaseError?, p1: DatabaseReference) {
                        if (error == null) {
                            isSuccess = true
                        }
                    }
                })
            return isSuccess
        }
    }

    fun readUserFromFireBase(userSignInModel: UserSignInModel) : String {
        var isError = "NA"
        val fireBaseReference = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(userSignInModel.userOutlet)
            .child(userSignInModel.userId)

        fireBaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                isError = error.toException().toString()
            }

            override fun onDataChange(snapShot: DataSnapshot) {
                if (snapShot.exists()) {
                    val model = snapShot.getValue(UserSignInModel::class.java)
                    if (model != null) {
                        val preferenceSharedUtils = PreferenceSharedUtils(context).getInstance()
                        with(model){
                            preferenceSharedUtils.setUserName(userName)
                            preferenceSharedUtils.setMobileNumber(userMobile)
                            preferenceSharedUtils.setOutletName(userOutlet)
                            preferenceSharedUtils.setSignInCode(verificationCode)
                            preferenceSharedUtils.setUserConfirmationStatus(codeVerified)
                            preferenceSharedUtils.setUserFireBaseChildId(userId)

                            preferenceSharedUtils.setUserSignInStatus(true)
                        }
                        isError = "SUCCESS"
                    }
                }
            }
        })
        return isError
    }
}