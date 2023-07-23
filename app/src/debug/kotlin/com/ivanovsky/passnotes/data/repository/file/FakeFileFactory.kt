package com.ivanovsky.passnotes.data.repository.file

import com.ivanovsky.passnotes.data.entity.FSAuthority
import com.ivanovsky.passnotes.data.entity.FileDescriptor
import com.ivanovsky.passnotes.util.FileUtils
import java.text.SimpleDateFormat
import java.util.Locale

class FakeFileFactory(
    private val fsAuthority: FSAuthority
) {

    fun createRootFile(): FileDescriptor {
        return create(fsAuthority, FileUid.ROOT, Time.ROOT)
    }

    fun createNoChangesFile(): FileDescriptor {
        return create(fsAuthority, FileUid.NO_CHANGES, Time.NO_CHANGES)
    }

    fun createRemoteChangesFile(): FileDescriptor {
        return create(fsAuthority, FileUid.REMOTE_CHANGES, Time.LOCAL)
    }

    fun createLocalChangesFile(): FileDescriptor {
        return create(fsAuthority, FileUid.LOCAL_CHANGES, Time.LOCAL)
    }

    fun createLocalChangesTimeoutFile(): FileDescriptor {
        return create(fsAuthority, FileUid.LOCAL_CHANGES_TIMEOUT, Time.LOCAL)
    }

    fun createConflictLocalFile(): FileDescriptor {
        return create(fsAuthority, FileUid.CONFLICT, Time.LOCAL)
    }

    fun createConflictRemoteFile(): FileDescriptor {
        return create(fsAuthority, FileUid.CONFLICT, Time.REMOTE)
    }

    fun createErrorFile(): FileDescriptor {
        return create(fsAuthority, FileUid.ERROR, Time.LOCAL)
    }

    fun createAuthErrorFile(): FileDescriptor {
        return create(fsAuthority, FileUid.AUTH_ERROR, Time.LOCAL)
    }

    fun createNotFoundFile(): FileDescriptor {
        return create(fsAuthority, FileUid.NOT_FOUND, Time.LOCAL)
    }

    private fun create(
        fsAuthority: FSAuthority,
        uid: String,
        modified: Long = System.currentTimeMillis()
    ): FileDescriptor {
        val path = pathFromUid(uid)

        return FileDescriptor(
            fsAuthority = fsAuthority,
            path = path,
            uid = uid,
            name = FileUtils.getFileNameFromPath(path),
            isDirectory = (uid == FileUid.ROOT),
            isRoot = (uid == FileUid.ROOT),
            modified = modified
        )
    }

    private fun pathFromUid(uid: String): String {
        return when (uid) {
            FileUid.ROOT -> "/"
            else -> "/test-$uid.kdbx"
        }
    }

    object FileUid {
        const val NO_CHANGES = "no-changes"
        const val ROOT = "/"
        const val REMOTE_CHANGES = "remote-changes"
        const val LOCAL_CHANGES = "local-changes"
        const val LOCAL_CHANGES_TIMEOUT = "local-changes-timeout"
        const val CONFLICT = "conflict"
        const val AUTH_ERROR = "auth-error"
        const val NOT_FOUND = "not-found"
        const val ERROR = "error"
    }

    private object Time {

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val ROOT = parseDate("2020-01-01")
        val NO_CHANGES = parseDate("2020-02-01")
        val LOCAL = parseDate("2020-03-01")
        val REMOTE = parseDate("2020-03-02")

        private fun parseDate(str: String): Long {
            return DATE_FORMAT.parse(str)?.time ?: throw IllegalArgumentException()
        }
    }
}