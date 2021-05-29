package com.ivanovsky.passnotes.data.repository.file.remote;

import com.ivanovsky.passnotes.data.entity.OperationError;
import com.ivanovsky.passnotes.data.entity.RemoteFile;
import com.ivanovsky.passnotes.data.entity.FileDescriptor;
import com.ivanovsky.passnotes.data.entity.OperationResult;
import com.ivanovsky.passnotes.data.entity.FSType;
import com.ivanovsky.passnotes.data.repository.file.FileSystemSyncProcessor;
import com.ivanovsky.passnotes.data.repository.file.OnConflictStrategy;
import com.ivanovsky.passnotes.data.repository.file.SyncStrategy;
import com.ivanovsky.passnotes.domain.FileHelper;
import com.ivanovsky.passnotes.extensions.RemoteFileExtKt;
import com.ivanovsky.passnotes.util.InputOutputUtils;
import com.ivanovsky.passnotes.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ivanovsky.passnotes.data.entity.OperationError.MESSAGE_FAILED_TO_ACCESS_TO_FILE;
import static com.ivanovsky.passnotes.data.entity.OperationError.MESSAGE_FAILED_TO_FIND_CACHED_FILE;
import static com.ivanovsky.passnotes.data.entity.OperationError.MESSAGE_FAILED_TO_FIND_FILE;
import static com.ivanovsky.passnotes.data.entity.OperationError.MESSAGE_LOCAL_VERSION_CONFLICTS_WITH_REMOTE;
import static com.ivanovsky.passnotes.data.entity.OperationError.newCacheError;
import static com.ivanovsky.passnotes.data.entity.OperationError.newDbVersionConflictError;
import static com.ivanovsky.passnotes.data.entity.OperationError.newFileAccessError;
import static com.ivanovsky.passnotes.data.entity.OperationError.newGenericIOError;
import static com.ivanovsky.passnotes.data.entity.OperationError.newNetworkIOError;
import static com.ivanovsky.passnotes.util.InputOutputUtils.newFileInputStreamOrNull;

public class RemoteFileSyncProcessor implements FileSystemSyncProcessor {

    private final RemoteFileSystemProvider provider;
    private final RemoteFileCache cache;
    private final FileHelper fileHelper;

    public RemoteFileSyncProcessor(RemoteFileSystemProvider provider,
                                   RemoteFileCache cache,
                                   FileHelper fileHelper) {
        this.provider = provider;
        this.cache = cache;
        this.fileHelper = fileHelper;
    }

    @Override
    public List<FileDescriptor> getLocallyModifiedFiles() {
        List<FileDescriptor> result = new ArrayList<>();

        List<RemoteFile> remoteFiles = cache.getLocallyModifiedFiles();
        for (RemoteFile file : remoteFiles) {
            FileDescriptor descriptor = RemoteFileExtKt.toFileDescriptor(file);
            result.add(descriptor);
        }

        return result;
    }

    @Override
    public OperationResult<FileDescriptor> process(FileDescriptor localDescriptor,
                                                   SyncStrategy syncStrategy,
                                                   OnConflictStrategy onConflictStrategy) {
        OperationResult<FileDescriptor> result = new OperationResult<>();

        RemoteFile cachedFile = cache.getByUid(localDescriptor.getUid());
        if (cachedFile != null) {
            OperationResult<FileDescriptor> getFileResult = provider.getFile(
                    localDescriptor.getPath(),
                    false);

            if (getFileResult.isSucceeded()) {
                FileDescriptor remoteDescriptor = getFileResult.getObj();

                Date localModified = new Date(localDescriptor.getModified());
                Date remoteModified = new Date(remoteDescriptor.getModified());

                if (syncStrategy == SyncStrategy.LAST_MODIFICATION_WINS) {
                    if (localModified.after(remoteModified)) {
                        result.from(uploadLocalFile(cachedFile, localDescriptor));

                    } else if (remoteModified.after(localModified)) {
                        if (onConflictStrategy == OnConflictStrategy.REWRITE) {
                            result.from(downloadFile(cachedFile, localDescriptor));

                        } else if (onConflictStrategy == OnConflictStrategy.CANCEL) {
                            result.setError(newDbVersionConflictError(MESSAGE_LOCAL_VERSION_CONFLICTS_WITH_REMOTE));
                        } else {
                            throw new IllegalArgumentException("Unsupported value: onConflictStrategy=" + onConflictStrategy);
                        }

                    } else {
                        // server and local version has the same time of modification
                        // pick up server version

                        result.from(downloadFile(cachedFile, localDescriptor));
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported value: syncStrategy=" + syncStrategy);
                }
            } else {
                result.setError(getFileResult.getError());
            }
        } else {
            result.setError(newCacheError(MESSAGE_FAILED_TO_FIND_CACHED_FILE));
        }

        return result;
    }

    private OperationResult<FileDescriptor> uploadLocalFile(RemoteFile cachedFile,
                                                            FileDescriptor localDescriptor) {
        OperationResult<FileDescriptor> result = new OperationResult<>();

        OperationResult<OutputStream> outResult = provider.openFileForWrite(localDescriptor,
                OnConflictStrategy.REWRITE,
                false);

        if (outResult.isSucceeded()) {
            OutputStream out = outResult.getObj();

            // Create buffer which will contains file data, because 'out' linked to the same file,
            // that 'in'

            OperationResult<InputStream> openBufferResult = copyFileAndOpen(new File(cachedFile.getLocalPath()));
            InputStream in = openBufferResult.getObj();

            try {
                InputOutputUtils.copy(in, out, true);

                RemoteFile updatedCachedFile = cache.getByUid(cachedFile.getUid());
                if (updatedCachedFile != null) {
                    result.setObj(RemoteFileExtKt.toFileDescriptor(updatedCachedFile));
                } else {
                    result.setError(newCacheError(MESSAGE_FAILED_TO_FIND_CACHED_FILE));
                }
            } catch (IOException e) {
                Logger.printStackTrace(e);
                result.setError(newNetworkIOError());
            }
        } else {
            result.setError(outResult.getError());
        }

        return result;
    }

    private OperationResult<InputStream> copyFileAndOpen(File file) {
        OperationResult<InputStream> result = new OperationResult<>();

        File buffer = fileHelper.generateDestinationFileForRemoteFile();
        if (buffer != null) {
            try {
                fileHelper.duplicateFile(file, buffer);

                InputStream in = newFileInputStreamOrNull(buffer);
                if (in != null) {
                    result.setObj(in);
                } else {
                    result.setError(newGenericIOError(MESSAGE_FAILED_TO_ACCESS_TO_FILE));
                }
            } catch (IOException e) {
                Logger.printStackTrace(e);

                result.setError(newGenericIOError(MESSAGE_FAILED_TO_ACCESS_TO_FILE));
            }
        } else {
            result.setError(newGenericIOError(MESSAGE_FAILED_TO_ACCESS_TO_FILE));
        }

        return result;
    }

    private OperationResult<FileDescriptor> downloadFile(RemoteFile cachedFile,
                                                         FileDescriptor localDescriptor) {
        OperationResult<FileDescriptor> result = new OperationResult<>();

        OperationResult<InputStream> inResult = provider.openFileForRead(localDescriptor,
                OnConflictStrategy.REWRITE,
                false);

        if (inResult.isSucceeded()) {
            InputStream in = inResult.getObj();

            try {
                in.close();

                RemoteFile updatedCachedFile = cache.getByUid(cachedFile.getUid());
                if (updatedCachedFile != null) {
                    result.setObj(RemoteFileExtKt.toFileDescriptor(updatedCachedFile));
                } else {
                    result.setError(newCacheError(MESSAGE_FAILED_TO_FIND_CACHED_FILE));
                }
            } catch (IOException e) {
                e.printStackTrace();
                result.setError(newFileAccessError(MESSAGE_FAILED_TO_ACCESS_TO_FILE));
            }
        } else {
            result.setError(inResult.getError());
        }

        return result;
    }
}