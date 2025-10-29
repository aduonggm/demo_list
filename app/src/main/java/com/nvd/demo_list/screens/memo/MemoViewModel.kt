package com.nvd.demo_list.screens.memo

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Date

class MemoViewModel(private val context: Context) : ViewModel() {
    private val repository = MemoRepository(context)
    
    private var _memos by mutableStateOf<List<Memo>>(emptyList())
    val memos: List<Memo> get() = _memos

    private var _isEditing by mutableStateOf(false)
    val isEditing: Boolean get() = _isEditing

    private var _editingMemo by mutableStateOf<Memo?>(null)
    val editingMemo: Memo? get() = _editingMemo
    
    init {
        loadMemos()
    }
    
    private fun loadMemos() {
        _memos = repository.loadMemos()
    }
    
    private fun saveMemos() {
        repository.saveMemos(_memos)
    }

    fun addMemo(content: String, dueDate: Date? = null) {
        if (content.isNotBlank()) {
            val newMemo = Memo(content = content.trim(), dueDate = dueDate)
            _memos = _memos + newMemo
            saveMemos()
        }
    }

    fun updateMemo(memo: Memo, newContent: String) {
        if (newContent.isNotBlank()) {
            _memos = _memos.map { 
                if (it.id == memo.id) {
                    it.copy(content = newContent.trim())
                } else {
                    it
                }
            }
            saveMemos()
        }
    }
    
    fun updateMemoWithDate(memo: Memo, newContent: String, dueDate: Date?) {
        if (newContent.isNotBlank()) {
            _memos = _memos.map { 
                if (it.id == memo.id) {
                    it.copy(content = newContent.trim(), dueDate = dueDate)
                } else {
                    it
                }
            }
            saveMemos()
        }
    }

    fun deleteMemo(memo: Memo) {
        _memos = _memos.filter { it.id != memo.id }
        saveMemos()
    }

    fun toggleMemoCompletion(memo: Memo) {
        _memos = _memos.map { 
            if (it.id == memo.id) {
                it.copy(isCompleted = !it.isCompleted)
            } else {
                it
            }
        }
        saveMemos()
    }

    fun startEditing(memo: Memo) {
        _editingMemo = memo
        _isEditing = true
    }

    fun cancelEditing() {
        _editingMemo = null
        _isEditing = false
    }

    fun finishEditing(newContent: String) {
        _editingMemo?.let { memo ->
            updateMemo(memo, newContent)
        }
        cancelEditing()
    }

    fun reorderMemo(fromIndex: Int, toIndex: Int) {
        if (fromIndex != toIndex && fromIndex in _memos.indices && toIndex in _memos.indices) {
            val newList = _memos.toMutableList()
            val item = newList.removeAt(fromIndex)
            newList.add(toIndex, item)
            _memos = newList
            saveMemos()
        }
    }
}
