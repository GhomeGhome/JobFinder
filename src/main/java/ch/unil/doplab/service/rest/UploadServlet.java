package ch.unil.doplab.service.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;

@WebServlet(urlPatterns = "/uploads/*")
public class UploadServlet extends HttpServlet {

    private File baseFolder;

    @Override
    public void init() throws ServletException {
        baseFolder = new File(System.getProperty("user.home"), "jobfinder_uploads");
        if (!baseFolder.exists()) baseFolder.mkdirs();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo(); // /{filename}
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        File target = new File(baseFolder, pathInfo.substring(1));
        if (!target.exists() || !target.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // naive content-type (PDF)
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + target.getName() + "\"");
        try (InputStream in = new FileInputStream(target);
             OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
        }
    }
}
