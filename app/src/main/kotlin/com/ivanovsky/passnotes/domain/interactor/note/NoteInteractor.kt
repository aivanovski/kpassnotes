package com.ivanovsky.passnotes.domain.interactor.note

import android.os.Handler
import android.os.Looper
import com.ivanovsky.passnotes.data.entity.Note
import com.ivanovsky.passnotes.data.entity.OperationResult
import com.ivanovsky.passnotes.data.repository.EncryptedDatabaseRepository
import com.ivanovsky.passnotes.domain.ClipboardHelper
import com.ivanovsky.passnotes.domain.entity.DatabaseStatus
import com.ivanovsky.passnotes.domain.usecases.LockDatabaseUseCase
import com.ivanovsky.passnotes.domain.usecases.GetDatabaseStatusUseCase
import java.util.*
import java.util.concurrent.TimeUnit

class NoteInteractor(
    private val dbRepo: EncryptedDatabaseRepository,
    private val clipboardHelper: ClipboardHelper,
    private val lockUseCase: LockDatabaseUseCase,
    private val getStatusUseCase: GetDatabaseStatusUseCase
) {

    fun getNoteByUid(noteUid: UUID): OperationResult<Note> {
        return dbRepo.noteRepository.getNoteByUid(noteUid)
    }

    fun copyToClipboardWithTimeout(text: String) {
        clipboardHelper.copy(text)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ clipboardHelper.clear() }, TimeUnit.SECONDS.toMillis(30))
    }

    fun getTimeoutValueInMillis(): Long {
        return TimeUnit.SECONDS.toMillis(30)
    }

    fun lockDatabase() {
        lockUseCase.lockIfNeed()
    }

    suspend fun getDatabaseStatus(): OperationResult<DatabaseStatus> =
        getStatusUseCase.getDatabaseStatus()
}