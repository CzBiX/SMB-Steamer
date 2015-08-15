package com.czbix.smbsteamer.helper;

import android.net.Uri;
import android.util.Log;

import com.czbix.smbsteamer.BuildConfig;
import com.czbix.smbsteamer.util.IoUtils;
import com.czbix.smbsteamer.util.SmbUtils;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public final class HttpServer extends NanoHTTPD {
    private static final String TAG = HttpServer.class.getSimpleName();

    public static final int PORT = 23333;
    public static final String URI_PREFIX = "/stream/";

    public HttpServer() {
        super(BuildConfig.DEBUG ? null : "127.0.0.1", PORT);
        setAsyncRunner(new ThreadPoolRunner());
    }

    @Override
    public Response serve(IHTTPSession session) {
        //noinspection LoopStatementThatDoesntLoop
        while (true) {
            final Method method = session.getMethod();
            String uri = session.getUri();
            Log.v(TAG, String.format("serve request: %s %s", method, uri));

            if (method != Method.GET) {
                break;
            }

            if (!uri.startsWith(URI_PREFIX)) {
                break;
            }

            uri = uri.substring(URI_PREFIX.length());
            return handleStream(uri, session);
        }

        return getForbiddenResponse();
    }

    private Response handleStream(String uri, IHTTPSession session) {
        SmbFile smbFile;
        try {
            smbFile = new SmbFile("smb://" + uri, NtlmPasswordAuthentication.ANONYMOUS);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Response response = null;
        try {
            if (smbFile.isFile()) {
                String mimeType = smbFile.getContentType();
                if (mimeType == null) {
                    mimeType = SmbUtils.getMineType(uri);
                }
                response = serveFile(session.getHeaders(), smbFile, mimeType);
            } else if (smbFile.getType() == SmbFile.TYPE_FILESYSTEM && smbFile.isDirectory()) {
                response = serveDir(smbFile);
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        }
        return response;
    }

    private Response serveFile(Map<String, String> header, SmbFile file, String mime) {
        Response res;
        boolean success = false;
        InputStream fis = null;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getCanonicalPath() + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && (ifNoneMatch.equals("*") || ifNoneMatch.equals(etag));

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();

            fis = file.getInputStream();
            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    ByteStreams.skipFully(fis, startFrom);

                    res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes */" + fileLen);
                    res.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
                    res = newFixedLengthResponse(Response.Status.OK, mime, fis, fileLen);
                    res.addHeader("Content-Length", Long.toString(fileLen));
                    res.addHeader("ETag", etag);
                }
            }
            success = true;
        } catch (IOException ioe) {
            res = getForbiddenResponse();
        } finally {
            if (!success && fis != null) {
                IoUtils.closeQuietly(fis);
            }
        }

        return res;
    }

    private Response serveDir(SmbFile file) throws SmbException, UnknownHostException {
        final String canonicalPath = file.getCanonicalPath();
        if (!canonicalPath.endsWith("/")) {
            // MX Player can't handle redirect
            try {
                file = new SmbFile(file, canonicalPath + "/");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        final SmbFile[] list = file.listFiles();
        StringBuilder sb = new StringBuilder();
        for (SmbFile f : list) {
            final String name = f.getName();
            sb.append(String.format("<a href=\"%s\">%s</a><br>", Uri.encode(name), name));
        }

        return newFixedLengthResponse(Response.Status.OK, MIME_HTML + "; charset=UTF-8",
                sb.toString());
    }

    protected Response getForbiddenResponse() {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Forbidden");
    }

    public static class ThreadPoolRunner implements AsyncRunner {
        private final ExecutorService mExecutor = Executors.newCachedThreadPool();
        private final List<ClientHandler> running = Collections.synchronizedList(new ArrayList<ClientHandler>());

        @Override
        public void closeAll() {
            // copy of the list for concurrency
            mExecutor.shutdown();
            for (ClientHandler clientHandler : Lists.newArrayList(running)) {
                clientHandler.close();
            }
        }

        @Override
        public void closed(ClientHandler clientHandler) {
            running.remove(clientHandler);
        }

        @Override
        public void exec(ClientHandler clientHandler) {
            running.add(clientHandler);
            mExecutor.submit(clientHandler);
        }
    }
}
