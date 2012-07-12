package com.adstream.servlets;

import com.sun.servicetag.UnauthorizedAccessException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import javax.servlet.http.*;
import java.io.*;

/**
 * A simple servlet that allows you to return a file within a file using Apache Commons VFS
 *
 * example call:
 * http://localhost:8080/files/path/to/somefile.zip!/path/to/somefile.jar!/path/to/index.txt
 *
 * Will return the file /path/to/index.txt within somefile.jar
 * where somefile.jar is the file /path/to/somefile.jar within somefile.zip
 * where somefile.zip is the file /path/to/somefile.zip relative to the filesystem directory defined in the servlet initParam FileStorePath
 */
public class VFSServlet extends HttpServlet {

    private String fileStorePath = null; //root directory on filesystem
    private StandardFileSystemManager fsManager;

    public void init() {
        fileStorePath = getServletConfig().getInitParameter("FileStorePath");

        fsManager = new StandardFileSystemManager();

        try {
            fsManager.setCacheStrategy(CacheStrategy.valueOf(getServletConfig().getInitParameter("CacheStrategy")));
            fsManager.init();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        InputStream in = null;
        OutputStream out = null;

        try {
            String vfsFilePath = request.getPathInfo();

            //check we are not trying to breach our directory boundary!
            if (vfsFilePath.contains("..")) throw new UnauthorizedAccessException();

            StringBuilder buff = new StringBuilder();

            //Determine what file extensions if any we need to drill in to
            int pos = 0, i;
            while((i = vfsFilePath.indexOf("!/", pos)) != -1) {
                buff.insert(0, FilenameUtils.getExtension(vfsFilePath.substring(pos, i)) + ':');
                pos = i+1;
            }

            //build up the required path for Apache Commons VFS
            buff.append("file://").append(fileStorePath).append(vfsFilePath);

            FileObject file = fsManager.resolveFile(buff.toString());
            String fileName = file.getName().getBaseName();

            in = file.getContent().getInputStream();
            out = response.getOutputStream();

            String mimetype = getServletContext().getMimeType(fileName);

            // sets response content type
            if (mimetype == null) {
                mimetype = "application/octet-stream";
            }

            response.setContentType(mimetype);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            IOUtils.copy(in, out);

            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            if (in != null) IOUtils.closeQuietly(in);
            if (out != null) IOUtils.closeQuietly(out);
        }
    }
}