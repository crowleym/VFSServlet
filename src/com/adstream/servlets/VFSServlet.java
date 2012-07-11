package com.adstream.servlets;

/**
 * Created with IntelliJ IDEA.
 * User: Mark.Crowley
 * Date: 11/07/12
 * Time: 15:01
 */

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.*;

import javax.servlet.http.*;
import java.io.*;

public class VFSServlet extends HttpServlet {

    private String fileStorePath = null;

    public void init() {
        fileStorePath = getServletConfig().getInitParameter("FileStorePath");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        FileSystemManager fsManager;
        InputStream in = null;
        OutputStream out = null;

        try {
            String vfsFilePath = request.getPathInfo();

            StringBuilder buff = new StringBuilder();

            int pos = 0, i;
            while((i = vfsFilePath.indexOf("!/", pos)) != -1) {
                buff.append(FilenameUtils.getExtension(vfsFilePath.substring(pos, i))).append(':');
                pos = i+1;
            }

            buff.append("file://").append(fileStorePath).append(vfsFilePath);

            fsManager = VFS.getManager();

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