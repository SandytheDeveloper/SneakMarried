package com.commonfriend.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.commonfriend.idrequest.ApiRepository

class ViewModelFactory(private val apiRepository: ApiRepository) :
    ViewModelProvider.NewInstanceFactory() { //NewInstanceFactory()


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> { // SignUpActivity
                return UserViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(QuestionViewModel::class.java) -> { // OnBoarding
                return QuestionViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(CheckListViewModel::class.java) -> { // CheckList
                return CheckListViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> { // Chat
                return ChatViewModel(apiRepository) as T
            }
            modelClass.isAssignableFrom(QuestionBankViewModel::class.java) -> { // QuestionBank
                return QuestionBankViewModel(apiRepository) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> { // ProfileView
                return ProfileViewModel(apiRepository) as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }


}