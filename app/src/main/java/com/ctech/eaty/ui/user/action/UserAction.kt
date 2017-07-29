package com.ctech.eaty.ui.user.action

import com.ctech.eaty.base.redux.Action

sealed class UserAction : Action() {
    data class Load(val id: Int) : UserAction()
    data class LoadRelationship(val userId: Int) : UserAction()

}