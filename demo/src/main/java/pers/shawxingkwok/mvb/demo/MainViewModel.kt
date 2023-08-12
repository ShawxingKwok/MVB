package pers.shawxingkwok.mvb.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(){
    //  from database in real cases
    private val _msgsFlow = MutableStateFlow(emptyList<Msg>())
    val msgsFlow: Flow<List<Msg>> get() = _msgsFlow

    fun sendMsg(text: String){

    }

    init {
        this
    }
}