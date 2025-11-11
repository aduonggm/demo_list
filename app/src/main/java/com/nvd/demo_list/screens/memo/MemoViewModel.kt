package com.nvd.demo_list.screens.memo

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Date

class MemoViewModel(private val context: Context) : ViewModel() {
    private val repository = MemoRepository(context)
    private val imageManager = com.nvd.demo_list.utils.ImageManager(context)
    
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
        val loadedMemos = repository.loadMemos()
        // Migrate any content URIs to app storage
        _memos = loadedMemos.map { memo ->
            if (memo.imageUris.any { imageManager.isContentUri(it) }) {
                val migratedUris = memo.imageUris.map { uriString ->
                    if (imageManager.isContentUri(uriString)) {
                        // Try to migrate content URI to app storage
                        try {
                            val sourceUri = Uri.parse(uriString)
                            val persistentUri = imageManager.copyUriToAppStorage(sourceUri)
                            persistentUri?.toString() ?: uriString
                        } catch (e: Exception) {
                            e.printStackTrace()
                            uriString
                        }
                    } else {
                        uriString
                    }
                }
                memo.copy(imageUris = migratedUris)
            } else {
                memo
            }
        }
        // Save migrated memos if any were migrated
        if (_memos != loadedMemos) {
            saveMemos()
        }
    }
    
    private fun saveMemos() {
        repository.saveMemos(_memos)
    }

    fun addMemo(content: String, dueDate: Date? = null, imageUris: List<String> = emptyList()) {
        if (content.isNotBlank()) {
            val newMemo = Memo(content = content.trim(), dueDate = dueDate, imageUris = imageUris)
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
    
    fun updateMemoWithDate(memo: Memo, newContent: String, dueDate: Date?, imageUris: List<String>? = null) {
        if (newContent.isNotBlank()) {
            _memos = _memos.map { 
                if (it.id == memo.id) {
                    it.copy(
                        content = newContent.trim(), 
                        dueDate = dueDate,
                        imageUris = imageUris ?: it.imageUris
                    )
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

    fun deleteAllMemos() {
        _memos = emptyList()
        repository.clearMemos()
    }

    fun getShareText(): String {
        if (_memos.isEmpty()) {
            return "No memos to share"
        }
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return _memos.joinToString("\n\n") { memo ->
            val status = if (memo.isCompleted) "✓ Completed" else "○ Pending"
            val dueDateText = memo.dueDate?.let { "Due: ${dateFormat.format(it)}" } ?: ""
            val imageCount = if (memo.imageUris.isNotEmpty()) " [${memo.imageUris.size} image(s)]" else ""
            "$status\n${memo.content}\n$dueDateText$imageCount"
        }
    }
}
