package com.czbix.smbsteamer.helper;

import android.util.Log;

import com.czbix.smbsteamer.BuildConfig;
import com.czbix.smbsteamer.util.SmbUtils;
import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public final class HttpServer extends NanoHTTPD {
    private static final String TAG = HttpServer.class.getSimpleName();
    static {
        if (!BuildConfig.DEBUG) {
            Logger.getLogger(NanoHTTPD.class.getName()).setLevel(Level.OFF);
        }
    }

    public static final int PORT = 23333;
    public static final String URI_PREFIX = "/stream/";

    public HttpServer() {
        super(BuildConfig.DEBUG ? null : "127.0.0.1", PORT);
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
        Stopwatch stopWatch = null;
        if (BuildConfig.DEBUG) {
            stopWatch = Stopwatch.createStarted();
        }

        final SmbFile smbFile;
        try {
            smbFile = new SmbFile("smb://" + uri, NtlmPasswordAuthentication.ANONYMOUS);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String mimeType = smbFile.getContentType();
        if (mimeType == null) {
            mimeType = SmbUtils.getMineType(uri);
        }
        final Response response = serveFile(session.getHeaders(), smbFile, mimeType);

        if (BuildConfig.DEBUG) {
            assert stopWatch != null;
            final long elapsed = stopWatch.elapsed(TimeUnit.MILLISECONDS);
            Log.v(TAG, "elapsed time(ms): " + elapsed);
        }
        return response;
    }

    private Response serveFile(Map<String, String> header, SmbFile file, String mime) {
        Response res;
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

                    InputStream fis = file.getInputStream();
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
                    res = newFixedLengthResponse(Response.Status.OK, mime, file.getInputStream(), file.length());
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse();
        }

        return res;
    }

    protected Response getForbiddenResponse() {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Forbidden");
    }
}
