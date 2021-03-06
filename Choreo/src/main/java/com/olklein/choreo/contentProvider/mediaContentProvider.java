package com.olklein.choreo.contentProvider;



/**
 * Created by olklein on 13/03/2018.
 */

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.olklein.choreo.ListFragment;
import com.olklein.choreo.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;







/**
 * Manages documents and exposes them to the Android system for sharing.
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class mediaContentProvider extends DocumentsProvider {
    private static final String TAG = "ChoreoProvider";

    // Use these as the default columns to return information about a root if no specific
    // columns are requested in a query.
    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_AVAILABLE_BYTES
    };

    // Use these as the default columns to return information about a document if no specific
    // columns are requested in a query.
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };

    // No official policy on how many to return, but make sure you do limit the number of recent
    // and search results.
    private static final int MAX_SEARCH_RESULTS = 20;
    private static final int MAX_LAST_MODIFIED = 5;

    private static final String ROOT = "root";

    // A file object at the root of the file hierarchy.  Depending on your implementation, the root
    // does not need to be an existing file system directory.  For example, a tag-based document
    // provider might return a directory containing all tags, represented as child directories.
    private File mBaseDir;
    private File mCacheDir;


    @Override
    public boolean onCreate() {
        Log.v(TAG, "onCreate");

        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/Choreo";
        mBaseDir=new File(downloadPath);
        if(!mBaseDir.exists()){
            mBaseDir.mkdirs();
        }
        mCacheDir = getContext().getCacheDir();
        return true;
    }

    // BEGIN_INCLUDE(query_roots)
    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        Log.v(TAG, "queryRoots");

        // Create a cursor with either the requested fields, or the default projection.  This
        // cursor is returned to the Android system picker UI and used to display all roots from
        // this provider.
        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));

        // It's possible to have multiple roots (e.g. for multiple accounts in the same app) -
        // just add multiple cursor rows.
        // Construct one row for a root called "MyCloud".
        final MatrixCursor.RowBuilder row = result.newRow();

        row.add(Root.COLUMN_ROOT_ID, ROOT);
        row.add(Root.COLUMN_SUMMARY, getContext().getString(R.string.root_summary));

        // FLAG_SUPPORTS_CREATE means at least one directory under the root supports creating
        // documents.  FLAG_SUPPORTS_RECENTS means your application's most recently used
        // documents will show up in the "Recents" category.  FLAG_SUPPORTS_SEARCH allows users
        // to search all documents the application shares.
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE |
                Root.FLAG_SUPPORTS_RECENTS |
                Root.FLAG_SUPPORTS_SEARCH|
                Document.FLAG_DIR_PREFERS_GRID);

        // COLUMN_TITLE is the root title (e.g. what will be displayed to identify your provider).
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.app_name));

        // This document id must be unique within this provider and consistent across time.  The
        // system picker UI may save it and refer to it later.
        row.add(Root.COLUMN_DOCUMENT_ID, getDocIdForFile(mBaseDir));

        // The child MIME types are used to filter the roots and only present to the user roots
        // that contain the desired type somewhere in their file hierarchy.
        row.add(Root.COLUMN_MIME_TYPES, getChildMimeTypes(mBaseDir));
        row.add(Root.COLUMN_AVAILABLE_BYTES, mBaseDir.getFreeSpace());
        row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher);

        String rootID = getDocIdForFile(mBaseDir);
        Uri updatedUri = DocumentsContract.buildChildDocumentsUri(AUTHORITY, rootID);
        result.setNotificationUri(getContext().getContentResolver(),updatedUri);

        return result;
    }
    // END_INCLUDE(query_roots)

    // BEGIN_INCLUDE(query_recent_documents)
    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection)
            throws FileNotFoundException {
        Log.v(TAG, "queryRecentDocuments");

        // This example implementation walks a local file structure to find the most recently
        // modified files.  Other implementations might include making a network call to query a
        // server.

        // Create a cursor with the requested projection, or the default projection.
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));

        final File parent = getFileForDocId(rootId);

        // Create a queue to store the most recent documents, which orders by last modified.
        PriorityQueue<File> lastModifiedFiles = new PriorityQueue<File>(5, new Comparator<File>() {
            public int compare(File i, File j) {
                return Long.compare(i.lastModified(), j.lastModified());
            }
        });

        // Iterate through all files and directories in the file structure under the root.  If
        // the file is more recent than the least recently modified, add it to the queue,
        // limiting the number of results.
        final LinkedList<File> pending = new LinkedList<File>();

        // Start by adding the parent to the list of files to be processed
        pending.add(parent);

        // Do while we still have unexamined files
        while (!pending.isEmpty()) {
            // Take a file from the list of unprocessed files
            final File file = pending.removeFirst();
            if (file.isDirectory()) {
                // If it's a directory, add all its children to the unprocessed list
                Collections.addAll(pending, file.listFiles());
            } else {
                // If it's a file, add it to the ordered queue.
                lastModifiedFiles.add(file);
            }
        }

        // Add the most recent files to the cursor, not exceeding the max number of results.
        for (int i = 0; i < Math.min(MAX_LAST_MODIFIED + 1, lastModifiedFiles.size()); i++) {
            final File file = lastModifiedFiles.remove();
            includeFile(result, null, file);
        }
        return result;
    }
    // END_INCLUDE(query_recent_documents)

    // BEGIN_INCLUDE(query_search_documents)
    @Override
    public Cursor querySearchDocuments(String rootId, String query, String[] projection)
            throws FileNotFoundException {
        Log.v(TAG, "querySearchDocuments");

        // Create a cursor with the requested projection, or the default projection.
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        final File parent = getFileForDocId(rootId);

        // This example implementation searches file names for the query and doesn't rank search
        // results, so we can stop as soon as we find a sufficient number of matches.  Other
        // implementations might use other data about files, rather than the file name, to
        // produce a match; it might also require a network call to query a remote server.

        // Iterate through all files in the file structure under the root until we reach the
        // desired number of matches.
        final LinkedList<File> pending = new LinkedList<File>();

        // Start by adding the parent to the list of files to be processed
        pending.add(parent);

        // Do while we still have unexamined files, and fewer than the max search results
        while (!pending.isEmpty() && result.getCount() < MAX_SEARCH_RESULTS) {
            // Take a file from the list of unprocessed files
            final File file = pending.removeFirst();
            if (file.isDirectory()) {
                // If it's a directory, add all its children to the unprocessed list
                Collections.addAll(pending, file.listFiles());
            } else {
                // If it's a file and it matches, add it to the result cursor.
                if (file.getName().toLowerCase().contains(query)) {
                    includeFile(result, null, file);
                }
            }
        }
        return result;
    }
    // END_INCLUDE(query_search_documents)

    // BEGIN_INCLUDE(open_document_thumbnail)
    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint,
                                                     CancellationSignal signal)
            throws FileNotFoundException {
        final File file = getThumbnailFileForDocId(documentId,sizeHint);
        if (file != null){
            final ParcelFileDescriptor pfd =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            return new AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
        }else{
            final File file_in = getFileForDocId(documentId);
            final ParcelFileDescriptor pfd =
                    ParcelFileDescriptor.open(file_in, ParcelFileDescriptor.MODE_READ_ONLY);
            return new AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH );
        }
    }

    private File getThumbnailFileForDocId(String documentId, Point sizeHint) throws FileNotFoundException {
        final File file = getFileForDocId(documentId);
        final String mimeType = getTypeForFile(file);
        Log.d(TAG,"size "+sizeHint.x+"x"+sizeHint.y);
        Bitmap bitmap = ListFragment.getThumbnail(Uri.fromFile(file),mimeType);

        if (bitmap!= null) {
            Bitmap bmp = Bitmap.createScaledBitmap(bitmap, sizeHint.x/2, sizeHint.y/2, false);
            if (bmp != null) {
                //create a file to write bitmap data
                File f = new File(mCacheDir, "/Thumb_"+file.getName());
                try {
                    f.createNewFile();

                    //Convert bitmap to byte array
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] bitmapData = bos.toByteArray();

                    //write the bytes in file
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapData);
                    fos.flush();
                    fos.close();
                    return f;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
// END_INCLUDE(open_document_thumbnail)

    // BEGIN_INCLUDE(query_document)
    @Override
    public Cursor queryDocument(String documentId, String[] projection)
            throws FileNotFoundException {
        Log.v(TAG, "queryDocument");

        // Create a cursor with the requested projection, or the default projection.
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        includeFile(result, documentId, null);
        return result;
    }
    // END_INCLUDE(query_document)

    // BEGIN_INCLUDE(query_child_documents)
    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection,
                                      String sortOrder) throws FileNotFoundException {
        Log.v(TAG, "queryChildDocuments, parentDocumentId: " +
                parentDocumentId +
                " sortOrder: " +
                sortOrder);
        final File parent = getFileForDocId(parentDocumentId);

        final MatrixCursor result = new DirectoryCursor(
                resolveDocumentProjection(projection), parentDocumentId, parent);

        for (File file : parent.listFiles()) {
            includeFile(result, null, file);
        }
        return result;
    }
    // END_INCLUDE(query_child_documents)


    // BEGIN_INCLUDE(open_document)
    @Override
    public ParcelFileDescriptor openDocument(final String documentId, final String mode,
                                             CancellationSignal signal)
            throws FileNotFoundException {
        Log.v(TAG, "openDocument, mode: " + mode);
        // It's OK to do network operations in this method to download the document, as long as you
        // periodically check the CancellationSignal.  If you have an extremely large file to
        // transfer from the network, a better solution may be pipes or sockets
        // (see ParcelFileDescriptor for helper methods).

        final File file = getFileForDocId(documentId);
        final int accessMode = ParcelFileDescriptor.parseMode(mode);

        final boolean isWrite = (mode.indexOf('w') != -1);
        if (isWrite) {
            // Attach a close listener if the document is opened in write mode.
            try {
                Handler handler = new Handler(getContext().getMainLooper());
                return ParcelFileDescriptor.open(file, accessMode, handler,
                        new ParcelFileDescriptor.OnCloseListener() {
                            @Override
                            public void onClose(IOException e) {

                                // Update the file with the cloud server.  The client is done writing.
                                Log.i(TAG, "A file with id " + documentId + " has been closed!  Time to " +
                                        "update the server.");
                            }

                        });
            } catch (IOException e) {
                throw new FileNotFoundException("Failed to open document with id " + documentId +
                        " and mode " + mode);
            }
        } else {
            return ParcelFileDescriptor.open(file, accessMode);
        }
    }
    // END_INCLUDE(open_document)


    // BEGIN_INCLUDE(create_document)
    @Override
    public String createDocument(String documentId, String mimeType, String displayName)
            throws FileNotFoundException {

        Log.d(TAG, "createDocument");

        File parent = getFileForDocId(documentId);
        File file = new File(parent.getPath(), displayName);
        try {
            file.createNewFile();
            file.setWritable(true);
            file.setReadable(true);
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to create document with name " +
                    displayName +" and documentId " + documentId);
        }

        return getDocIdForFile(file);
    }
    // END_INCLUDE(create_document)

    private static final String AUTHORITY = "com.olklein.choreo.contentProvider.mediaContent";
    // BEGIN_INCLUDE(delete_document)
    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        Log.v(TAG, "deleteDocument");
        File file = getFileForDocId(documentId);

        if (file.delete()) {
            Log.i(TAG, "Deleted file with id " + documentId);

        } else {
            throw new FileNotFoundException("Failed to delete document with id " + documentId);
        }
    }
    // END_INCLUDE(delete_document)






    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {

        File file = getFileForDocId(documentId);
        return getTypeForFile(file);
    }

    /**
     * @param projection the requested root column projection
     * @return either the requested root column projection, or the default projection if the
     * requested projection is null.
     */
    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    /**
     * Get a file's MIME type
     *
     * @param file the File object whose type we want
     * @return the MIME type of the file
     */
    private static String getTypeForFile(File file) {
        if (file.isDirectory()) {
            return Document.MIME_TYPE_DIR;
        } else {
            return getTypeForName(file.getName());
        }
    }

    /**
     * Get the MIME data type of a document, given its filename.
     *
     * @param name the filename of the document
     * @return the MIME data type of a document
     */
    private static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                Log.d(TAG,name+"is "+mime);
                return mime;
            }
        }
        return "application/octet-stream";
    }

    /**
     * Gets a string of unique MIME data types a directory supports, separated by newlines.  This
     * should not change.
     *
     * @param parent the File for the parent directory
     * @return a string of the unique MIME data types the parent directory supports
     */
    private String getChildMimeTypes(File parent) {
        Set<String> mimeTypes = new HashSet<String>();
        mimeTypes.add("image/*");
        mimeTypes.add("text/*");
        mimeTypes.add("audio/*");
        mimeTypes.add("video/*");
        mimeTypes.add("application/pdf");
        mimeTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        // Flatten the list into a string and insert newlines between the MIME type strings.
        StringBuilder mimeTypesString = new StringBuilder();
        for (String mimeType : mimeTypes) {
            mimeTypesString.append(mimeType).append("\n");
        }

        return mimeTypesString.toString();
    }

    /**
     * Get the document ID given a File.  The document id must be consistent across time.  Other
     * applications may save the ID and use it to reference documents later.
     * <p/>
     * This implementation is specific to this demo.  It assumes only one root and is built
     * directly from the file structure.  However, it is possible for a document to be a child of
     * multiple directories (for example "android" and "images"), in which case the file must have
     * the same consistent, unique document ID in both cases.
     *
     * @param file the File whose document ID you want
     * @return the corresponding document ID
     */
    private String getDocIdForFile(File file) {
        String path = file.getAbsolutePath();

        // Start at first char of path under root
        final String rootPath = mBaseDir.getPath();
        if (rootPath.equals(path)) {
            path = "";
        } else if (rootPath.endsWith("/")) {
            path = path.substring(rootPath.length());
        } else {
            path = path.substring(rootPath.length() + 1);
        }

        return "root" + ':' + path;
    }


    /**
     * Add a representation of a file to a cursor.
     *
     * @param result the cursor to modify
     * @param docId  the document ID representing the desired file (may be null if given file)
     * @param file   the File object representing the desired file (may be null if given docID)
     * @throws java.io.FileNotFoundException
     */
    private void includeFile(MatrixCursor result, String docId, File file)
            throws FileNotFoundException {
        if (docId == null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }

        int flags = 0;

        if (file.isDirectory()) {
            // Request the folder to lay out as a grid rather than a list. This also allows a larger
            // thumbnail to be displayed for each image.
            flags |= Document.FLAG_DIR_PREFERS_GRID;

            // Add FLAG_DIR_SUPPORTS_CREATE if the file is a writable directory.
            if (file.isDirectory() && file.canWrite()) {
                flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
            }
        } else if (file.canWrite()) {
            // If the file is writable set FLAG_SUPPORTS_WRITE and
            // FLAG_SUPPORTS_DELETE
            flags |= Document.FLAG_SUPPORTS_WRITE;
            flags |= Document.FLAG_SUPPORTS_DELETE;

        }

        final String displayName = file.getName();
        final String mimeType = getTypeForFile(file);

        if (mimeType.startsWith("image/") || mimeType.startsWith("video/")
                || mimeType.startsWith("audio/") || mimeType.startsWith("application/pdf")) {
            // Allow the image to be represented by a thumbnail rather than an icon
            flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, displayName);
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(Document.COLUMN_FLAGS, flags);

        // Add a custom icon
        row.add(Document.COLUMN_ICON, R.mipmap.ic_launcher);
    }

    /**
     * Translate your custom URI scheme into a File object.
     *
     * @param docId the document ID representing the desired file
     * @return a File represented by the given document ID
     * @throws java.io.FileNotFoundException
     */
    private File getFileForDocId(String docId) throws FileNotFoundException {
        File target = mBaseDir;


        if (docId.equals(ROOT)) {
            return target;
        }
        final int splitIndex = docId.indexOf(':', 1);
        if (splitIndex < 0) {
            throw new FileNotFoundException("Missing root for " + docId);
        } else {
            final String path = docId.substring(splitIndex + 1);

            target = new File(target, path);

            if (!target.exists()) {
                throw new FileNotFoundException("Missing file for " + docId + " at " + target);
            }
            return target;
        }
    }

    private void startObserving(File file, Uri notifyUri) {
        synchronized (mObservers) {
            DirectoryObserver observer = mObservers.get(file);
            if (observer == null) {
                observer = new DirectoryObserver(
                        file, getContext().getContentResolver(), notifyUri);
                observer.startWatching();
                mObservers.put(file, observer);
            }
            observer.mRefCount++;

            Log.d(TAG, "after start: " + observer);
        }
    }
    private void stopObserving(File file) {
        synchronized (mObservers) {
            DirectoryObserver observer = mObservers.get(file);
            if (observer == null) return;
            observer.mRefCount--;
            if (observer.mRefCount == 0) {
                mObservers.remove(file);
                observer.stopWatching();
            }
            Log.d(TAG, "after stop: " + observer);
        }
    }
    private Map<File, DirectoryObserver> mObservers = new HashMap<File, DirectoryObserver>();


    private static class DirectoryObserver extends FileObserver {
        private static final int NOTIFY_EVENTS = ATTRIB | CLOSE_WRITE | MOVED_FROM | MOVED_TO
                | CREATE | DELETE | DELETE_SELF | MOVE_SELF;

        private final File mFile;
        private final ContentResolver mResolver;
        private final Uri mNotifyUri;

        private int mRefCount = 0;

        public DirectoryObserver(File file, ContentResolver resolver, Uri notifyUri) {
            super(file.getAbsolutePath(), NOTIFY_EVENTS);
            mFile = file;
            mResolver = resolver;
            mNotifyUri = notifyUri;
        }

        @Override
        public void onEvent(int event, String path) {
            if ((event & NOTIFY_EVENTS) != 0) {
                Log.d(TAG, "onEvent() " + event + " at " + path);
                mResolver.notifyChange(mNotifyUri, null, false);
            }
        }

        @Override
        public String toString() {
            return "DirectoryObserver{file=" + mFile.getAbsolutePath() + ", ref=" + mRefCount + "}";
        }
    }

    private class DirectoryCursor extends MatrixCursor {
        private final File mFile;

        public DirectoryCursor(String[] columnNames, String docId, File file) {
            super(columnNames);

            final Uri notifyUri = DocumentsContract.buildChildDocumentsUri(
                    AUTHORITY, docId);
            setNotificationUri(getContext().getContentResolver(), notifyUri);

            mFile = file;
            startObserving(mFile, notifyUri);
        }

        @Override
        public void close() {
            super.close();
            stopObserving(mFile);
        }
    }


}
