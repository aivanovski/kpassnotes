package com.ivanovsky.passnotes.data.repository.file.regular

import com.ivanovsky.passnotes.data.entity.ConflictResolutionStrategy
import com.ivanovsky.passnotes.data.entity.FileDescriptor
import com.ivanovsky.passnotes.data.entity.OperationError.MESSAGE_INCORRECT_USE_CASE
import com.ivanovsky.passnotes.data.entity.OperationError.newGenericError
import com.ivanovsky.passnotes.data.entity.OperationResult
import com.ivanovsky.passnotes.data.entity.SyncConflictInfo
import com.ivanovsky.passnotes.data.entity.SyncStatus
import com.ivanovsky.passnotes.data.repository.file.FileSystemSyncProcessor
import com.ivanovsky.passnotes.data.repository.file.SyncStrategy

class RegularFileSystemSyncProcessor : FileSystemSyncProcessor {

    override fun getLocallyModifiedFiles(): MutableList<FileDescriptor> {
        return mutableListOf()
    }

    override fun getSyncStatusForFile(uid: String): SyncStatus {
        return SyncStatus.NO_CHANGES
    }

    override fun getSyncConflictForFile(uid: String): OperationResult<SyncConflictInfo> {
        return OperationResult.error(newGenericError(MESSAGE_INCORRECT_USE_CASE))
    }

    override fun process(
        file: FileDescriptor,
        syncStrategy: SyncStrategy,
        resolutionStrategy: ConflictResolutionStrategy?
    ): OperationResult<FileDescriptor> {
        return OperationResult.error(newGenericError(MESSAGE_INCORRECT_USE_CASE))
    }
}